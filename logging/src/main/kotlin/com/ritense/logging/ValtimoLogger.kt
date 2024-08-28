/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.logging

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.Marker

class ValtimoLogger(
    val slf4jLogger: Logger,
    val objectMapper: ObjectMapper
) {
    /**
     * Log a message at the TRACE level.
     *
     * @param msg the message string to be logged
     * @since 1.4
     */
    fun trace(msg: () -> String?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.trace(msg.invoke())
        removeMDCValues()
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and argument.
     *
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level.
     *
     * @param format the format string
     * @param arg    the argument
     * @since 1.4
     */
    fun trace(format: String?, parameters: Map<String, Any>? = null, arg: Any?) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.trace(format, arg)
        removeMDCValues()
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     *
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @since 1.4
     */
    fun trace(format: String?, parameters: Map<String, Any>? = null, arg1: Any?, arg2: Any?) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.trace(format, arg1, arg2)
        removeMDCValues()
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     *
     *
     * This form avoids superfluous string concatenation when the logger
     * is disabled for the TRACE level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an `Object[]` before invoking the method,
     * even if this logger is disabled for TRACE. The variants taking [one][.trace] and
     * [two][.trace] arguments exist solely in order to avoid this hidden cost.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     * @since 1.4
     */
    fun trace(format: String?, parameters: Map<String, Any>? = null, vararg arguments: Any?) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.trace(format, arguments)
        removeMDCValues()
    }

    /**
     * Log an exception (throwable) at the TRACE level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     * @since 1.4
     */
    fun trace(msg: () -> String?, t: Throwable?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(t = t, parameters = parameters)
        slf4jLogger.trace(msg.invoke(), t)
        removeMDCValues()
    }

    /**
     * Log a message with the specific Marker at the TRACE level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message string to be logged
     * @since 1.4
     */
    fun trace(marker: Marker?, msg: () -> String?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.trace(marker, msg.invoke())
        removeMDCValues()
    }

    /**
     * This method is similar to [.trace] method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     * @since 1.4
     */
    fun trace(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, arg: Any?) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.trace(marker, format, arg)
        removeMDCValues()
    }

    /**
     * This method is similar to [.trace]
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     * @since 1.4
     */
    fun trace(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, arg1: Any?, arg2: Any?) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.trace(marker, format, arg1, arg2)
        removeMDCValues()
    }

    /**
     * This method is similar to [.trace]
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker   the marker data specific to this log statement
     * @param format   the format string
     * @param argArray an array of arguments
     * @since 1.4
     */
    fun trace(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, vararg argArray: Any?) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.trace(marker, format, argArray)
        removeMDCValues()
    }

    /**
     * This method is similar to [.trace] method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     * @since 1.4
     */
    fun trace(marker: Marker?, msg: () -> String?, t: Throwable?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(t = t, parameters = parameters)
        slf4jLogger.trace(marker, msg.invoke(), t)
        removeMDCValues()
    }

    /**
     * Log a message at the DEBUG level.
     *
     * @param msg the message string to be logged
     */
    fun debug(msg: () -> String?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.debug(msg.invoke())
        removeMDCValues()
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and argument.
     *
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    fun debug(format: String?, parameters: Map<String, Any>? = null, arg: Any?) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.debug(format, arg)
        removeMDCValues()
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     *
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    fun debug(format: String?, parameters: Map<String, Any>? = null, arg1: Any?, arg2: Any?) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.debug(format, arg1, arg2)
        removeMDCValues()
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     *
     *
     * This form avoids superfluous string concatenation when the logger
     * is disabled for the DEBUG level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an `Object[]` before invoking the method,
     * even if this logger is disabled for DEBUG. The variants taking
     * [one][.debug] and [two][.debug]
     * arguments exist solely in order to avoid this hidden cost.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    fun debug(format: String?, parameters: Map<String, Any>? = null, vararg arguments: Any?) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.debug(format, arguments)
        removeMDCValues()
    }

    /**
     * Log an exception (throwable) at the DEBUG level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    fun debug(msg: () -> String?, t: Throwable?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(t = t, parameters = parameters)
        slf4jLogger.debug(msg.invoke(), t)
        removeMDCValues()
    }

    /**
     * Log a message with the specific Marker at the DEBUG level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message string to be logged
     */
    fun debug(marker: Marker?, msg: () -> String?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.debug(marker, msg.invoke())
        removeMDCValues()
    }

    /**
     * This method is similar to [.debug] method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    fun debug(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, arg: Any?) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.debug(marker, format, arg)
        removeMDCValues()
    }

    /**
     * This method is similar to [.debug]
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    fun debug(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, arg1: Any?, arg2: Any?) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.debug(marker, format, arg1, arg2)
        removeMDCValues()
    }

    /**
     * This method is similar to [.debug]
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker    the marker data specific to this log statement
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    fun debug(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, vararg arguments: Any?) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.debug(marker, format, arguments)
        removeMDCValues()
    }

    /**
     * This method is similar to [.debug] method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    fun debug(marker: Marker?, msg: () -> String?, t: Throwable?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isDebugEnabled) return
        putMDCValues(t = t, parameters = parameters)
        slf4jLogger.debug(marker, msg.invoke(), t)
        removeMDCValues()
    }

    /**
     * Log a message at the INFO level.
     *
     * @param msg the message string to be logged
     */
    fun info(msg: () -> String?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isInfoEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.info(msg.invoke())
        removeMDCValues()
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and argument.
     *
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the INFO level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    fun info(format: String?, parameters: Map<String, Any>? = null, arg: Any?) {
        if (!slf4jLogger.isInfoEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.info(format, arg)
        removeMDCValues()
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     *
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the INFO level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    fun info(format: String?, parameters: Map<String, Any>? = null, arg1: Any?, arg2: Any?) {
        if (!slf4jLogger.isInfoEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.info(format, arg1, arg2)
        removeMDCValues()
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     *
     *
     * This form avoids superfluous string concatenation when the logger
     * is disabled for the INFO level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an `Object[]` before invoking the method,
     * even if this logger is disabled for INFO. The variants taking
     * [one][.info] and [two][.info]
     * arguments exist solely in order to avoid this hidden cost.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    fun info(format: String?, parameters: Map<String, Any>? = null, vararg arguments: Any?) {
        if (!slf4jLogger.isInfoEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.info(format, arguments)
        removeMDCValues()
    }

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    fun info(msg: () -> String?, t: Throwable?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isInfoEnabled) return
        putMDCValues(t = t, parameters = parameters)
        slf4jLogger.info(msg.invoke(), t)
        removeMDCValues()
    }

    /**
     * Log a message with the specific Marker at the INFO level.
     *
     * @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     */
    fun info(marker: Marker?, msg: () -> String?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isInfoEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.info(marker, msg.invoke())
        removeMDCValues()
    }

    /**
     * This method is similar to [.info] method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    fun info(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, arg: Any?) {
        if (!slf4jLogger.isInfoEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.info(marker, format, arg)
        removeMDCValues()
    }

    /**
     * This method is similar to [.info]
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    fun info(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, arg1: Any?, arg2: Any?) {
        if (!slf4jLogger.isInfoEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.info(marker, format, arg1, arg2)
        removeMDCValues()
    }

    /**
     * This method is similar to [.info]
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker    the marker data specific to this log statement
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    fun info(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, vararg arguments: Any?) {
        if (!slf4jLogger.isInfoEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.info(marker, format, arguments)
        removeMDCValues()
    }

    /**
     * This method is similar to [.info] method
     * except that the marker data is also taken into consideration.
     *
     * @param marker the marker data for this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    fun info(marker: Marker?, msg: () -> String?, t: Throwable?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isInfoEnabled) return
        putMDCValues(t = t, parameters = parameters)
        slf4jLogger.info(marker, msg.invoke(), t)
        removeMDCValues()
    }

    /**
     * Log a message at the WARN level.
     *
     * @param msg the message string to be logged
     */
    fun warn(msg: () -> String?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isWarnEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.warn(msg.invoke())
        removeMDCValues()
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and argument.
     *
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the WARN level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    fun warn(format: String?, parameters: Map<String, Any>? = null, arg: Any?) {
        if (!slf4jLogger.isWarnEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.warn(format, arg)
        removeMDCValues()
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     *
     *
     * This form avoids superfluous string concatenation when the logger
     * is disabled for the WARN level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an `Object[]` before invoking the method,
     * even if this logger is disabled for WARN. The variants taking
     * [one][.warn] and [two][.warn]
     * arguments exist solely in order to avoid this hidden cost.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    fun warn(format: String?, parameters: Map<String, Any>? = null, vararg arguments: Any?) {
        if (!slf4jLogger.isWarnEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.warn(format, arguments)
        removeMDCValues()
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     *
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the WARN level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    fun warn(format: String?, parameters: Map<String, Any>? = null, arg1: Any?, arg2: Any?) {
        if (!slf4jLogger.isWarnEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.warn(format, arg1, arg2)
        removeMDCValues()
    }

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    fun warn(msg: () -> String?, t: Throwable?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isWarnEnabled) return
        putMDCValues(t = t, parameters = parameters)
        slf4jLogger.warn(msg.invoke(), t)
        removeMDCValues()
    }

    /**
     * Log a message with the specific Marker at the WARN level.
     *
     * @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     */
    fun warn(marker: Marker?, msg: () -> String?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isWarnEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.warn(marker, msg.invoke())
        removeMDCValues()
    }

    /**
     * This method is similar to [.warn] method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    fun warn(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, arg: Any?) {
        if (!slf4jLogger.isWarnEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.warn(marker, format, arg)
        removeMDCValues()
    }

    /**
     * This method is similar to [.warn]
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    fun warn(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, arg1: Any?, arg2: Any?) {
        if (!slf4jLogger.isWarnEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.warn(marker, format, arg1, arg2)
        removeMDCValues()
    }

    /**
     * This method is similar to [.warn]
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker    the marker data specific to this log statement
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    fun warn(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, vararg arguments: Any?) {
        if (!slf4jLogger.isWarnEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.warn(marker, format, arguments)
        removeMDCValues()
    }

    /**
     * This method is similar to [.warn] method
     * except that the marker data is also taken into consideration.
     *
     * @param marker the marker data for this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    fun warn(marker: Marker?, msg: () -> String?, t: Throwable?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isWarnEnabled) return
        putMDCValues(t = t, parameters = parameters)
        slf4jLogger.warn(marker, msg.invoke(), t)
        removeMDCValues()
    }

    /**
     * Log a message at the ERROR level.
     *
     * @param msg the message string to be logged
     */
    fun error(msg: () -> String?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isErrorEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.error(msg.invoke())
        removeMDCValues()
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and argument.
     *
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    fun error(format: String?, parameters: Map<String, Any>? = null, arg: Any?) {
        if (!slf4jLogger.isErrorEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.error(format, arg)
        removeMDCValues()
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     *
     *
     * This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    fun error(format: String?, parameters: Map<String, Any>? = null, arg1: Any?, arg2: Any?) {
        if (!slf4jLogger.isErrorEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.error(format, arg1, arg2)
        removeMDCValues()
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     *
     *
     * This form avoids superfluous string concatenation when the logger
     * is disabled for the ERROR level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an `Object[]` before invoking the method,
     * even if this logger is disabled for ERROR. The variants taking
     * [one][.error] and [two][.error]
     * arguments exist solely in order to avoid this hidden cost.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    fun error(format: String?, parameters: Map<String, Any>? = null, vararg arguments: Any?) {
        if (!slf4jLogger.isErrorEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.error(format, arguments)
        removeMDCValues()
    }

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    fun error(msg: () -> String?, t: Throwable?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isErrorEnabled) return
        putMDCValues(t = t, parameters = parameters)
        slf4jLogger.error(msg.invoke(), t)
        removeMDCValues()
    }

    /**
     * Log a message with the specific Marker at the ERROR level.
     *
     * @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     */
    fun error(marker: Marker?, msg: () -> String?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isErrorEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.error(marker, msg.invoke())
        removeMDCValues()
    }

    /**
     * This method is similar to [.error] method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    fun error(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, arg: Any?) {
        if (!slf4jLogger.isErrorEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.error(marker, format, arg)
        removeMDCValues()
    }

    /**
     * This method is similar to [.error]
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    fun error(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, arg1: Any?, arg2: Any?) {
        if (!slf4jLogger.isErrorEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.error(marker, format, arg1, arg2)
        removeMDCValues()
    }

    /**
     * This method is similar to [.error]
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker    the marker data specific to this log statement
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    fun error(marker: Marker?, format: String?, parameters: Map<String, Any>? = null, vararg arguments: Any?) {
        if (!slf4jLogger.isErrorEnabled) return
        putMDCValues(parameters = parameters)
        slf4jLogger.error(marker, format, arguments)
        removeMDCValues()
    }

    /**
     * This method is similar to [.error]
     * method except that the marker data is also taken into
     * consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    fun error(marker: Marker?, msg: () -> String?, t: Throwable?, parameters: Map<String, Any>? = null) {
        if (!slf4jLogger.isErrorEnabled) return
        putMDCValues(t = t, parameters = parameters)
        slf4jLogger.error(marker, msg.invoke(), t)
        removeMDCValues()
    }

    private fun putMDCValues(t: Throwable? = null, parameters: Map<String, Any>? = null) {
        t?.let {
            MDC.put(MDC_EXCEPTION_CLASS_KEY, t.javaClass.name)
            MDC.put(MDC_STACKTRACE_KEY, t.stackTraceToString())
        }

        if (slf4jLogger.isTraceEnabled && parameters != null) {
            MDC.put(MDC_PARAMETERS_KEY, objectMapper.writeValueAsString(parameters))
        }

        ResourceLoggerContext.putMDCResources()
    }

    private fun removeMDCValues() {
        MDC.remove(MDC_EXCEPTION_CLASS_KEY)
        MDC.remove(MDC_STACKTRACE_KEY)
        MDC.remove(MDC_PARAMETERS_KEY)
        ResourceLoggerContext.removeMDCResources()
    }


    companion object {
        const val MDC_EXCEPTION_CLASS_KEY = "exceptionClass"
        const val MDC_STACKTRACE_KEY = "stacktrace"
        const val MDC_PARAMETERS_KEY = "parameters"

        fun getLogger(clazz: Class<*>, objectMapper: ObjectMapper): ValtimoLogger {
            return ValtimoLogger(LoggerFactory.getLogger(clazz), objectMapper)
        }

        fun getLogger(slf4jLogger: Logger, objectMapper: ObjectMapper): ValtimoLogger {
            return ValtimoLogger(slf4jLogger, objectMapper)
        }
    }
}