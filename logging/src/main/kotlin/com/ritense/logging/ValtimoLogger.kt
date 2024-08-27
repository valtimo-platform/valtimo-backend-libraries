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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker

class ValtimoLogger (
    val slf4jLogger: Logger
) {
    /**
     * Log a message at the TRACE level.
     *
     * @param msg the message string to be logged
     * @since 1.4
     */
    fun trace(msg: String?) {
        slf4jLogger.trace(msg)
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
    fun trace(format: String?, arg: Any?) {
        slf4jLogger.trace(format, arg)
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
    fun trace(format: String?, arg1: Any?, arg2: Any?) {
        slf4jLogger.trace(format, arg1, arg2)
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
    fun trace(format: String?, vararg arguments: Any?) {
        slf4jLogger.trace(format, arguments)
    }

    /**
     * Log an exception (throwable) at the TRACE level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     * @since 1.4
     */
    fun trace(msg: String?, t: Throwable?) {
        slf4jLogger.trace(msg, t)
    }

    /**
     * Log a message with the specific Marker at the TRACE level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message string to be logged
     * @since 1.4
     */
    fun trace(marker: Marker?, msg: String?) {
        slf4jLogger.trace(marker, msg)
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
    fun trace(marker: Marker?, format: String?, arg: Any?) {
        slf4jLogger.trace(marker, format, arg)
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
    fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        slf4jLogger.trace(marker, format, arg1, arg2)
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
    fun trace(marker: Marker?, format: String?, vararg argArray: Any?) {
        slf4jLogger.trace(marker, format, argArray)
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
    fun trace(marker: Marker?, msg: String?, t: Throwable?) {
        slf4jLogger.trace(marker, msg, t)
    }

    /**
     * Log a message at the DEBUG level.
     *
     * @param msg the message string to be logged
     */
    fun debug(msg: String?) {
        slf4jLogger.debug(msg)
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
    fun debug(format: String?, arg: Any?) {
        slf4jLogger.debug(format, arg)
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
    fun debug(format: String?, arg1: Any?, arg2: Any?) {
        slf4jLogger.debug(format, arg1, arg2)
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
    fun debug(format: String?, vararg arguments: Any?) {
        slf4jLogger.debug(format, arguments)
    }

    /**
     * Log an exception (throwable) at the DEBUG level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    fun debug(msg: String?, t: Throwable?) {
        slf4jLogger.debug(msg, t)
    }

    /**
     * Log a message with the specific Marker at the DEBUG level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message string to be logged
     */
    fun debug(marker: Marker?, msg: String?) {
        slf4jLogger.debug(marker, msg)
    }

    /**
     * This method is similar to [.debug] method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    fun debug(marker: Marker?, format: String?, arg: Any?) {
        slf4jLogger.debug(marker, format, arg)
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
    fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        slf4jLogger.debug(marker, format, arg1, arg2)
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
    fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {
        slf4jLogger.debug(marker, format, arguments)
    }

    /**
     * This method is similar to [.debug] method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    fun debug(marker: Marker?, msg: String?, t: Throwable?) {
        slf4jLogger.debug(marker, msg, t)
    }

    /**
     * Log a message at the INFO level.
     *
     * @param msg the message string to be logged
     */
    fun info(msg: String?) {
        slf4jLogger.info(msg)
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
    fun info(format: String?, arg: Any?) {
        slf4jLogger.info(format, arg)
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
    fun info(format: String?, arg1: Any?, arg2: Any?) {
        slf4jLogger.info(format, arg1, arg2)
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
    fun info(format: String?, vararg arguments: Any?) {
        slf4jLogger.info(format, arguments)
    }

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    fun info(msg: String?, t: Throwable?) {
        slf4jLogger.info(msg, t)
    }

    /**
     * Log a message with the specific Marker at the INFO level.
     *
     * @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     */
    fun info(marker: Marker?, msg: String?) {
        slf4jLogger.info(marker, msg)
    }

    /**
     * This method is similar to [.info] method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    fun info(marker: Marker?, format: String?, arg: Any?) {
        slf4jLogger.info(marker, format, arg)
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
    fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        slf4jLogger.info(marker, format, arg1, arg2)
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
    fun info(marker: Marker?, format: String?, vararg arguments: Any?) {
        slf4jLogger.info(marker, format, arguments)

    }

    /**
     * This method is similar to [.info] method
     * except that the marker data is also taken into consideration.
     *
     * @param marker the marker data for this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    fun info(marker: Marker?, msg: String?, t: Throwable?) {
        slf4jLogger.info(marker, msg, t)
    }

    /**
     * Log a message at the WARN level.
     *
     * @param msg the message string to be logged
     */
    fun warn(msg: String?) {
        slf4jLogger.warn(msg)
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
    fun warn(format: String?, arg: Any?) {
        slf4jLogger.warn(format, arg)
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
    fun warn(format: String?, vararg arguments: Any?) {
        slf4jLogger.warn(format, arguments)
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
    fun warn(format: String?, arg1: Any?, arg2: Any?) {
        slf4jLogger.warn(format, arg1, arg2)
    }

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    fun warn(msg: String?, t: Throwable?) {
        slf4jLogger.warn(msg, t)
    }

    /**
     * Similar to [.isWarnEnabled] method except that the marker
     * data is also taken into consideration.
     *
     * @param marker The marker data to take into consideration
     * @return True if this Logger is enabled for the WARN level,
     * false otherwise.
     */
    fun isWarnEnabled(marker: Marker?): Boolean {
        return slf4jLogger.isWarnEnabled(marker)
    }

    /**
     * Log a message with the specific Marker at the WARN level.
     *
     * @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     */
    fun warn(marker: Marker?, msg: String?) {
        slf4jLogger.warn(marker, msg)
    }

    /**
     * This method is similar to [.warn] method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    fun warn(marker: Marker?, format: String?, arg: Any?) {
        slf4jLogger.warn(marker, format, arg)
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
    fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        slf4jLogger.warn(marker, format, arg1, arg2)
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
    fun warn(marker: Marker?, format: String?, vararg arguments: Any?) {
        slf4jLogger.warn(marker, format, arguments)
    }

    /**
     * This method is similar to [.warn] method
     * except that the marker data is also taken into consideration.
     *
     * @param marker the marker data for this log statement
     * @param msg    the message accompanying the exception
     * @param t      the exception (throwable) to log
     */
    fun warn(marker: Marker?, msg: String?, t: Throwable?) {
        slf4jLogger.warn(marker, msg, t)
    }

    /**
     * Log a message at the ERROR level.
     *
     * @param msg the message string to be logged
     */
    fun error(msg: String?) {
        slf4jLogger.error(msg)
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
    fun error(format: String?, arg: Any?) {
        slf4jLogger.error(format, arg)
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
    fun error(format: String?, arg1: Any?, arg2: Any?) {
        slf4jLogger.error(format, arg1, arg2)
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
    fun error(format: String?, vararg arguments: Any?) {
        slf4jLogger.error(format, arguments)
    }

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    fun error(msg: String?, t: Throwable?) {
        slf4jLogger.error(msg, t)
    }

    /**
     * Log a message with the specific Marker at the ERROR level.
     *
     * @param marker The marker specific to this log statement
     * @param msg    the message string to be logged
     */
    fun error(marker: Marker?, msg: String?) {
        slf4jLogger.error(marker, msg)
    }

    /**
     * This method is similar to [.error] method except that the
     * marker data is also taken into consideration.
     *
     * @param marker the marker data specific to this log statement
     * @param format the format string
     * @param arg    the argument
     */
    fun error(marker: Marker?, format: String?, arg: Any?) {
        slf4jLogger.error(marker, format, arg)
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
    fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        slf4jLogger.error(marker, format, arg1, arg2)
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
    fun error(marker: Marker?, format: String?, vararg arguments: Any?) {
        slf4jLogger.error(marker, format, arguments)
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
    fun error(marker: Marker?, msg: String?, t: Throwable?) {
        slf4jLogger.error(marker, msg, t)
    }


    companion object {
        fun getLogger(clazz: Class<*>): ValtimoLogger {
            return ValtimoLogger(LoggerFactory.getLogger(clazz))
        }

        fun getLogger(slf4jLogger: Logger): ValtimoLogger {
            return ValtimoLogger(slf4jLogger)
        }
    }
}