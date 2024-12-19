package com.example.amanuensis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import com.example.amanuensis.model.NotabilityListEntry
import com.example.amanuensis.ui.theme.AmanuensisTheme
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.EOFException
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.math.max

// TODO: comment code even slightly, possibly, maybe
// TODO: get rid of duplicate code

class MainActivity : ComponentActivity() {

    private val USER_DATA_SAVE_FILENAME: String = "user_data.txt"
    private var userInputCollectionJson: JsonObject = JsonObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmanuensisApp()
        }

        userInputCollectionJson = loadUserInputDataFromInternalStorage()
    }

    @Preview(showBackground = true)
    @Composable
    fun AmanuensisApp() {
        AmanuensisTheme(darkTheme = false) {
            var calcForAll: Boolean by rememberSaveable { mutableStateOf(true) }
            var requiredMakingWaves: Int by rememberSaveable { mutableIntStateOf(0) }

            val makingWavesText = stringResource(id = R.string.making_waves_input)
            val bizarreText = stringResource(id = R.string.bizarre_input)
            val dreadedText = stringResource(id = R.string.dreaded_input)
            val respectableText = stringResource(id = R.string.respectable_input)

            var textFieldMW: Int by rememberSaveable { mutableIntStateOf(userInputCollectionJson.get(makingWavesText)?.asInt ?:0) }
            var textFieldB: Int by rememberSaveable {mutableIntStateOf(userInputCollectionJson.get(bizarreText)?.asInt ?:0) }
            var textFieldD: Int by rememberSaveable { mutableIntStateOf(userInputCollectionJson.get(dreadedText)?.asInt ?:0)  }
            var textFieldR: Int by rememberSaveable { mutableIntStateOf(userInputCollectionJson.get(respectableText)?.asInt ?:0)  }

            val focusManager = LocalFocusManager.current

            Column(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .clickable(
                    indication = null,
                    interactionSource = remember {
                        MutableInteractionSource()
                    }
                ) {
                    focusManager.clearFocus()
                }
            ) {
                Header(
                    title = stringResource(id = R.string.header_title),
                    subTitle = stringResource(id = R.string.header_subtitle),
                    modifier = Modifier
                        .padding(8.dp)
                )

                BackgroundCard {
                    InputFieldsAndButtons(
                        calcForAll,
                        requiredMakingWaves,
                        focusManager,
                        textFieldB,
                        textFieldD,
                        textFieldR,
                        textFieldMW,
                        { requiredMakingWaves = it },
                        { calcForAll = it },
                        { textFieldB = it },
                        { textFieldD = it },
                        { textFieldR = it },
                        { textFieldMW = it }
                    )
                }

                if (calcForAll) {
                    BackgroundCard {
                        NotabilityRequirementsList(textFieldB, textFieldD, textFieldR, textFieldMW)
                    }
                }
            }
        }
    }

    @Composable
    fun BackgroundCard(content: @Composable () -> Unit) {
        Surface(
            modifier = Modifier
                .defaultMinSize(minWidth = 60.dp, minHeight = 60.dp)
                .padding(6.dp)
                .background(MaterialTheme.colorScheme.surface),
            shape = RectangleShape,
            tonalElevation = 8.dp
        ) {
            Surface(
                modifier = Modifier
                    .defaultMinSize(minWidth = 60.dp, minHeight = 60.dp)
                    .padding(10.dp)
                    .border(BorderStroke(2.dp, MaterialTheme.colorScheme.outline))
            ) {
                content()
            }
        }
    }

    @Composable
    fun Header(title: String, subTitle: String, modifier: Modifier = Modifier) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                lineHeight = 29.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subTitle,
                fontSize = 12.sp,
                lineHeight = 29.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }

    @Composable
    fun InputFieldsAndButtons(
        calcForAll: Boolean,
        requiredMakingWaves: Int,
        focusManager: FocusManager,
        bizarre: Int,
        dreaded: Int,
        respectable: Int,
        makingWaves: Int,
        updateMakingWaves: (Int) -> Unit,
        onCheckboxPress: (Boolean) -> Unit,
        getBizarre: (Int) -> Unit,
        getDreaded: (Int) -> Unit,
        getRespectable: (Int) -> Unit,
        getMakingWaves: (Int) -> Unit,
    ) {
        Column {
            InputFields(
                modifier = Modifier
                    .fillMaxWidth(),
                calcForAll,
                focusManager,
                bizarre,
                dreaded,
                respectable,
                makingWaves,
                getBizarre,
                getDreaded,
                getRespectable,
                getMakingWaves,
                onCheckboxPress,
            )
            {
                updateMakingWaves(it)
            }
            if (!calcForAll) {
                Text(
                    text = stringResource(id = R.string.making_waves_requirement) + " " + requiredMakingWaves,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
                Text(
                    text = stringResource(id = R.string.making_waves_cp_requirement) + " " + calculateMakingWavesCP(
                        requiredMakingWaves
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    @Composable
    fun InputFields(
        modifier: Modifier = Modifier,
        calcForAll: Boolean,
        focusManager: FocusManager,
        bizarre: Int,
        dreaded: Int,
        respectable: Int,
        makingWaves: Int,
        getBizarre: (Int) -> Unit,
        getDreaded: (Int) -> Unit,
        getRespectable: (Int) -> Unit,
        getMakingWaves: (Int) -> Unit,
        onCheckboxPress: (Boolean) -> Unit,
        onButtonPress: (Int) -> Unit,
    ) {
        val textFieldWidth = LocalConfiguration.current.screenWidthDp.dp / 4f

        val notabilityText = stringResource(id = R.string.notability_input)
        var textFieldN: Int by rememberSaveable { mutableIntStateOf(userInputCollectionJson.get(notabilityText)?.asInt ?:0) }
        var textFieldMW: Int by rememberSaveable { mutableIntStateOf(makingWaves) }
        var textFieldB: Int by rememberSaveable {mutableIntStateOf(bizarre) }
        var textFieldD: Int by rememberSaveable { mutableIntStateOf(dreaded) }
        var textFieldR: Int by rememberSaveable { mutableIntStateOf(respectable) }

        val focusRequester = remember {
            FocusRequester()
        }

        Column(
            modifier = modifier.clickable(
                indication = null,
                interactionSource = remember {
                    MutableInteractionSource()
                }
            ) {
                focusManager.clearFocus()
            },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                InputTextField(
                    textFieldB.toString(),
                    stringResource(id = R.string.bizarre_input),
                    textFieldWidth,
                    true,
                    Modifier.focusRequester(focusRequester),
                    { focusManager.moveFocus(FocusDirection.Right) }
                ) {
                    getBizarre(it)
                    textFieldB = it
                }
                InputTextField(
                    textFieldD.toString(),
                    stringResource(id = R.string.dreaded_input),
                    textFieldWidth,
                    true,
                    Modifier.focusRequester(focusRequester),
                    { focusManager.moveFocus(FocusDirection.Right) }
                ) {
                    getDreaded(it)
                    textFieldD = it
                }
                InputTextField(
                    textFieldR.toString(),
                    stringResource(id = R.string.respectable_input),
                    textFieldWidth,
                    true,
                    Modifier.focusRequester(focusRequester),
                    { focusManager.moveFocus(FocusDirection.Down) }
                ) {
                    getRespectable(it)
                    textFieldR = it
                }
            }
            InputTextField(
                textFieldMW.toString(),
                stringResource(id = R.string.making_waves_input),
                textFieldWidth * 3f,
                true,
                Modifier.focusRequester(focusRequester),
                { if (!calcForAll) focusManager.moveFocus(FocusDirection.Down) else focusManager.clearFocus() }
            ) {
                getMakingWaves(it)
                textFieldMW = it
            }
            InputTextField(
                textFieldN.toString(),
                stringResource(id = R.string.notability_input),
                textFieldWidth * 3f,
                !calcForAll,
                Modifier.focusRequester(focusRequester),
                { focusManager.clearFocus() }
            ) {
                textFieldN = it
            }

            if (!calcForAll) {
                OutlinedButton(
                    onClick = {
                        onButtonPress(
                            calculateMakingWaves(
                                textFieldN,
                                textFieldB,
                                textFieldD,
                                textFieldR,
                                textFieldMW
                            )
                        )
                        focusRequester.freeFocus()
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .focusRequester(focusRequester)
                ) {
                    Text(
                        text = stringResource(R.string.calc_making_waves)
                    )
                }
            }

            CheckboxWithText(
                text = stringResource(id = R.string.checbox_info_text),
                modifier = Modifier,
                fontWeight = FontWeight.Normal,
                lineHeight = 1.sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                initialChecked = calcForAll
            ) { onCheckboxPress(it) }
        }
    }

    @Composable
    fun InputTextField(
        textValue: String,
        label: String,
        width: Dp,
        visible: Boolean,
        modifier: Modifier,
        onNext: () -> Unit,
        onChange: (Int) -> Unit
    ) {
        var textFieldValue by remember {
            mutableStateOf(
                TextFieldValue(
                    text = textValue,
                    selection = TextRange(textValue.length)
                )
            )
        }
        var hasFocus by remember { mutableStateOf(false) }
        var initialAction by remember { mutableStateOf(true) }

        val maxChar = 3

        if (visible) {
            TextField(
                value = textFieldValue,
                singleLine = true,
                onValueChange = { newValue ->
                    if (newValue.text.length <= maxChar) {
                        if (newValue.text == "0" && hasFocus && initialAction) {
                            textFieldValue = TextFieldValue("", selection = TextRange(1))
                            initialAction = false
                        } else {
                            if (newValue.text.isDigitsOnly()) textFieldValue = newValue
                            initialAction = false
                        }
                        val returnValue = if (newValue.text != "" && newValue.text.isDigitsOnly()) newValue.text.toInt() else 0
                        onChange(
                            returnValue
                        )
                        userInputCollectionJson.addProperty(label, returnValue.toString())
                        saveUserInputDataToInternalStorage()
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        onNext()
                    }
                ),
                label = { Text(label, fontSize = 8.sp) },
                modifier = modifier
                    .requiredWidth(width)
                    .padding(4.dp)
                    .onFocusChanged {
                        hasFocus = it.isFocused
                        if (it.isFocused) {
                            if (textFieldValue.text == "0") {
                                textFieldValue = TextFieldValue("", selection = TextRange(1))
                            }
                        } else {
                            if (textFieldValue.text == "") {
                                textFieldValue = TextFieldValue("0", selection = TextRange(1))
                            }
                            initialAction = true
                        }
                    }
            )
        }
    }

/*
                    Text(
                        text = "Notability ${listEntry.notability}",
                        fontWeight = FontWeight.Bold,
                        lineHeight = 29.sp,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = modifier
                    )
 */

    @Composable
    fun CheckboxWithText(
        text: String,
        modifier: Modifier = Modifier,
        fontWeight: FontWeight = FontWeight.Normal,
        lineHeight: TextUnit = 1.sp,
        color: Color = Color.Unspecified,
        textAlign: TextAlign = TextAlign.Start,
        initialChecked: Boolean = false,
        onChange: (Boolean) -> Unit
    ) {
        var checked by rememberSaveable { mutableStateOf(initialChecked) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
        ) {
            Text(
                text = text,
                fontWeight = fontWeight,
                lineHeight = lineHeight,
                color = color,
                textAlign = textAlign,
                modifier = modifier
            )
            Checkbox(
                checked = checked,
                onCheckedChange = {
                    checked = it
                    onChange(it)
                },
                modifier = modifier
            )
        }
    }

    @Composable
    fun NotabilityRequirementsList(bizarre: Int, dreaded: Int, respectable: Int, makingWaves: Int) {
        val list = mutableListOf<NotabilityListEntry>()
        val maxNotability = 15
        for (i in 0..<maxNotability) {
            val makingWavesRequired =
                calculateMakingWaves(i, bizarre, dreaded, respectable, makingWaves)
            val changePointsRequired = calculateMakingWavesCP(makingWavesRequired)
            list.add(NotabilityListEntry(i + 1, makingWavesRequired, changePointsRequired))
        }
        LazyColumn {
            items(list) { item ->
                NotabilityListCard(
                    item, Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
    @Composable
    fun NotabilityListCard(listEntry: NotabilityListEntry,
                           modifier: Modifier = Modifier) {

        var isChecked by rememberSaveable {
            mutableStateOf(userInputCollectionJson.get("Notability ${listEntry.notability}")?.asString.toBoolean())
        }
        Card(
            modifier = modifier
                .padding(8.dp)
        ) {
            Column {
                CheckboxWithText(
                    text = "Notability ${listEntry.notability}",
                    modifier = Modifier,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 29.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onBackground,
                    initialChecked = isChecked
                ) {
                    isChecked = it
                    userInputCollectionJson.addProperty("Notability ${listEntry.notability}", it.toString())
                    saveUserInputDataToInternalStorage()
                }
                NotabilityListCardText(
                    text = stringResource(id = R.string.making_waves_requirement),
                    makingWavesRequired = if(isChecked) "-" else listEntry.makingWavesRequired.toString(),
                    modifier = modifier
                )
                NotabilityListCardText(
                    text = stringResource(id = R.string.making_waves_cp_requirement),
                    makingWavesRequired = if(isChecked) "-" else listEntry.changePointsRequired.toString() ,
                    modifier = modifier
                )
            }
        }
    }

    @Composable
    fun NotabilityListCardText(text: String, makingWavesRequired: String, modifier: Modifier) {
        Row {
            Text(
                buildAnnotatedString {
                    withStyle(style = ParagraphStyle(textAlign = TextAlign.Start)) {
                        append(text)
                    }
                }
            )
            Text(
                buildAnnotatedString {
                    withStyle(style = ParagraphStyle(textAlign = TextAlign.End)) {
                        append(makingWavesRequired)
                    }
                },
                modifier = modifier
            )
        }
    }

    private fun calculateMakingWaves(
        notability: Int,
        bizarre: Int,
        dreaded: Int,
        respectable: Int,
        makingWaves: Int
    ): Int {
        return max(0, 20 - (bizarre + dreaded + respectable) + 4 * notability - makingWaves)
    }

    private fun calculateMakingWavesCP(num: Int): Int {
        var result = 0
        for (i in 1..num) result += i
        return result
    }

    private fun saveUserInputDataToInternalStorage() : Boolean {

        return try {

            openFileOutput(USER_DATA_SAVE_FILENAME, MODE_PRIVATE).use {
                //val array = JsonArray().toString().toByteArray()
                val array = userInputCollectionJson.toString().toByteArray()
                it.write(array)
                //Toast.makeText(this, "Marker saved", Toast.LENGTH_SHORT).show()
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun loadUserInputDataFromInternalStorage() : JsonObject {
        return try {
            val markersFile: String
            openFileInput(USER_DATA_SAVE_FILENAME).use {
                val byteArray = ByteArray(it.available())
                it.read(byteArray)
                markersFile = byteArray.toString(Charsets.UTF_8)
                //Toast.makeText(this, "Markers loaded", Toast.LENGTH_SHORT).show()
            }
            JsonParser.parseString(markersFile).asJsonObject
        } catch (e: IOException) {
            e.printStackTrace()
            JsonObject()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            JsonObject()
        }
        catch (e: EOFException) {
            e.printStackTrace()
            JsonObject()
        }
    }

}

