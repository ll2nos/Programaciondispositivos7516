package com.sebastianllano.calculadoranomina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sebastianllano.calculadoranomina.ui.theme.CalculadoraNominaTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CalculadoraNominaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    CalculadoraNominaApp()
                }
            }
        }
    }
}

@Composable
fun CalculadoraNominaApp() {
    var salarioInput by rememberSaveable { mutableStateOf("") }
    var horasDiurnasInput by rememberSaveable { mutableStateOf("") }
    var horasNocturnasInput by rememberSaveable { mutableStateOf("") }
    var esDominical by rememberSaveable { mutableStateOf(false) }
    var transporteEmpresa by rememberSaveable { mutableStateOf(false) }

    var resultado by rememberSaveable { mutableStateOf<ResultadoNomina?>(null) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var errorField by rememberSaveable { mutableStateOf<Int?>(null) } // 1 para salario, 2 para horas

    fun limpiar() {
        salarioInput = ""
        horasDiurnasInput = ""
        horasNocturnasInput = ""
        esDominical = false
        transporteEmpresa = false
        resultado = null
        errorMessage = null
        errorField = null
    }

    fun calcular() {
        errorMessage = null
        errorField = null

        val salario = salarioInput.toDoubleOrNull()
        if (salario == null) {
            errorMessage = "Ingrese un salario válido"
            errorField = 1
            resultado = null
            return
        }

        if (salario < SMMLV_2026) {
            errorMessage = "El salario no puede ser inferior al mínimo ($ 1.750.905)"
            errorField = 1
            resultado = null
            return
        }

        val diurnas = horasDiurnasInput.toDoubleOrNull() ?: 0.0
        val nocturnas = horasNocturnasInput.toDoubleOrNull() ?: 0.0

        if (horasDiurnasInput.isNotEmpty() && diurnas < 0.0 || horasNocturnasInput.isNotEmpty() && nocturnas < 0.0) {
            errorMessage = "Las horas extra deben ser un número mayor o igual a cero"
            errorField = 2
            resultado = null
            return
        }
        if (diurnas < 0.0 || nocturnas < 0.0) {
            errorMessage = "Las horas extra deben ser un número mayor o igual a cero"
            errorField = 2
            resultado = null
            return
        }

        if ((diurnas + nocturnas) > 48.0) {
            errorMessage = "El total de horas extra no puede superar 48 en el mes"
            errorField = 2
            resultado = null
            return
        }

        resultado = calcularNomina(
            salarioBasico = salario,
            horasDiurnas = diurnas,
            horasNocturnas = nocturnas,
            esDominical = esDominical,
            transporteEmpresa = transporteEmpresa
        )
    }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // campo para meter el salario
        CampoNumerico(
            etiqueta = R.string.salario_basico,
            valor = salarioInput,
            onValueChange = { salarioInput = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            isError = errorField == 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // horas extra diurnas
        CampoNumerico(
            etiqueta = R.string.horas_extra_diurnas,
            valor = horasDiurnasInput,
            onValueChange = { horasDiurnasInput = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            isError = errorField == 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // horas extra nocturnas
        CampoNumerico(
            etiqueta = R.string.horas_extra_nocturnas,
            valor = horasNocturnasInput,
            onValueChange = { horasNocturnasInput = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            isError = errorField == 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // si es dominical o festivo
        FilaInterruptor(
            etiqueta = R.string.horas_dominicales,
            checked = esDominical,
            onCheckedChange = {
                esDominical = it
                if (resultado != null) calcular()
            },
            modifier = Modifier.fillMaxWidth()
        )

        // transporte que da la empresa
        FilaInterruptor(
            etiqueta = R.string.transporte_empresa,
            checked = transporteEmpresa,
            onCheckedChange = {
                transporteEmpresa = it
                if (resultado != null) calcular()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // por si hay algun error
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.Start)
            )
        }

        // botones de calcular y limpiar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { calcular() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text(stringResource(R.string.calcular), color = Color.White)
            }
            OutlinedButton(
                onClick = { limpiar() },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.limpiar))
            }
        }

        // aqui se muestran los resultados
        resultado?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // imagen segun el rango
                    val (imageRes, descRes, rangoTextRes) = when (res.rango) {
                        RangoSalarial.RANGO_1 -> Triple(R.drawable.rango_1, R.string.content_desc_rango_1, R.string.rango_1_desc)
                        RangoSalarial.RANGO_2 -> Triple(R.drawable.rango_2, R.string.content_desc_rango_2, R.string.rango_2_desc)
                        RangoSalarial.RANGO_3 -> Triple(R.drawable.rango_3, R.string.content_desc_rango_3, R.string.rango_3_desc)
                    }

                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = stringResource(id = descRes),
                        modifier = Modifier
                            .size(64.dp)
                            .padding(bottom = 8.dp)
                    )

                    Text(
                        text = stringResource(id = rangoTextRes),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = stringResource(R.string.resultado_titulo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    FilaDetalle(stringResource(R.string.valor_hora_ordinaria), formatearMoneda(res.valorHora))
                    FilaDetalle(stringResource(R.string.salario_basico_label), formatearMoneda(res.salarioBasico))
                    FilaDetalle(stringResource(R.string.horas_extra_label), formatearMoneda(res.totalHorasExtra))
                    FilaDetalle(stringResource(R.string.auxilio_transporte_label), formatearMoneda(res.auxilioTransporte))
                    FilaDetalle(stringResource(R.string.total_devengado_label), formatearMoneda(res.totalDevengado), isBold = true)

                    Spacer(modifier = Modifier.height(8.dp))

                    FilaDetalle(stringResource(R.string.salud_label), formatearMoneda(res.aporteSalud))
                    FilaDetalle(stringResource(R.string.pension_label), formatearMoneda(res.aportePension))
                    FilaDetalle(stringResource(R.string.fondo_solidaridad_label), formatearMoneda(res.fondoSolidaridad))
                    FilaDetalle(stringResource(R.string.total_deducciones_label), formatearMoneda(res.totalDeducciones), isBold = true)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${stringResource(R.string.salario_neto_label)}:\n${formatearMoneda(res.salarioNeto)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // cuantos salarios minimos equivalen
                    Text(
                        text = stringResource(R.string.equivalencia_smmlv, String.format(Locale.US, "%.2f", res.equivalenciaSmmv)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sebastian Llano",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF000000)
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CampoNumerico(
    @StringRes etiqueta: Int,
    valor: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(stringResource(etiqueta)) },
        singleLine = true,
        isError = isError,
        keyboardOptions = keyboardOptions,
        modifier = modifier
    )
}

@Composable
fun FilaInterruptor(
    @StringRes etiqueta: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(etiqueta),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun FilaDetalle(concepto: String, valor: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = concepto,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CalculadoraNominaPreview() {
    CalculadoraNominaTheme {
        CalculadoraNominaApp()
    }
}
