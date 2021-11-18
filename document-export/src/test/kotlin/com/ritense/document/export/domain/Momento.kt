/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.document.export.domain

class TextEditor(private var textWindow: TextWindow) {
    private var savedTextWindow: TextWindowState? = null

    fun write(text: String) {
        textWindow.addText(text)
        println("write:$text")
    }

    fun print(): String {
        return textWindow.getCurrentText()
    }

    fun hitSave() {
        savedTextWindow = textWindow.save()
        println("save:" + print())
    }

    fun hitUndo() {
        savedTextWindow?.let { textWindow.restore(it) }
    }

}

class TextWindow {
    private var currentText: StringBuilder = StringBuilder()

    fun getCurrentText(): String {
        return currentText.toString()
    }

    fun addText(text: String) {
        currentText.append(text)
    }

    fun save(): TextWindowState {
        return TextWindowState(currentText.toString())
    }

    fun restore(state: TextWindowState) {
        currentText = StringBuilder(state.text)
    }

}

class TextWindowState(val text: String)