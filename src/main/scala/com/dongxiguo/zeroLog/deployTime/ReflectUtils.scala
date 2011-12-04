/*
 * Copyright 2011 杨博 (Yang Bo)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dongxiguo.zeroLog
package deployTime

import java.lang.reflect.Method
import scala.annotation.tailrec

object ReflectUtils {
  // Eat my own dog food
  private val (logger, formatter) = ZeroLoggerFactory.newLogger(this)
  import formatter._

  private val ParentPackagePattern = """^(?:(.*)\.|)\w+$"""r

  @tailrec
  final def searchClass(packageName: String,
                        className: String): Class[_] = {
    packageName match {
      case null | "" =>
        Class.forName(className)
      case ParentPackagePattern(parentPackageName) =>
        try {
          return Class.forName(packageName + "." + className)
        } catch {
          case e: ClassNotFoundException =>
            logger.fine{ _ append packageName append '.' append className append
                        " is not found.  Will try its parent." }
        }
        searchClass(parentPackageName, className)
      case _ =>
        throw new IllegalArgumentException("Bad package name: " + packageName)
    }
  }

  private def addAllSuper(
    builder: collection.mutable.Builder[Class[_], Set[Class[_]]],
    c: Class[_]) {
    builder += c
    c.getSuperclass match {
      case null =>
      case s =>
        addAllSuper(builder, s)
    }
    for (i <- c.getInterfaces)
      addAllSuper(builder, i)
  }


  private def getAllSuper(c: Class[_]): Set[Class[_]] = {
    val builder = Set.newBuilder[Class[_]]
    addAllSuper(builder, c)
    builder.result
  }

  final def findBestMatchingStaticMethod(
    clazz: Class[_], methodName: String,
    parameterTypes: Class[_]*): Method = {
    clazz.getMethods filter { m =>
      m.getName == methodName &&
      m.getParameterTypes.corresponds(parameterTypes) {
        (declearing, expected) =>
        if (declearing == expected) {
          return m // perfect matching
        } else {
          declearing.isAssignableFrom(expected)
        }
      }
    } match {
      case Array() =>
        throw new NoSuchMethodException
      case Array(onlyOne) => onlyOne
      case multipleMethods =>
        throw new NoSuchMethodException(
          "Multiple matched methods: " + multipleMethods)
    }
  }

  final def invokeStatic[Parameter <: AnyRef : Manifest](
    packageName: String, className: String, methodName: String,
    parameter: Parameter): AnyRef = {
    findBestMatchingStaticMethod(
      searchClass(packageName, className),
      methodName,
      manifest[Parameter].erasure).invoke(null, parameter)
  }

}
