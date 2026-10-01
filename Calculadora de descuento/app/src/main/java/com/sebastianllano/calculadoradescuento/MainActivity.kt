package com.sebastianllano.calculadoradescuento

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sebastianllano.calculadoradescuento.ui.theme.CalculadoraDescuentoTheme
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CalculadoraDescuentoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    CalculadoraDescuentoApp()
                }
            }
        }
    }
}

@Composable
fun CalculadoraDescuentoApp() {
    // variables para guardar lo que escribe el usuario
    var precioInput by rememberSaveable { mutableStateOf("") }
    var descuentoInput by rememberSaveable { mutableStateOf("") }

    // resultados del calculo
    var valorDescuento by rememberSaveable { mutableStateOf<Double?>(null) }
    var totalPagar by rememberSaveable { mutableStateOf<Double?>(null) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // limpiar campos
    fun limpiar() {
        precioInput = ""
        descuentoInput = ""
        valorDescuento = null
        totalPagar = null
        errorMessage = null
    }

    // calcular descuento
    fun calcular() {
        errorMessage = null

        val precio = precioInput.toDoubleOrNull()
        if (precio == null || precio <= 0.0) {
            errorMessage = "ingrese un precio valido"
            valorDescuento = null
            totalPagar = null
            return
        }

        val porcentaje = descuentoInput.toDoubleOrNull()
        if (porcentaje == null || porcentaje < 0.0 || porcentaje > 100.0) {
            errorMessage = "ingrese un porcentaje entre 0 y 100"
            valorDescuento = null
            totalPagar = null
            return
        }

        val desc = precio * (porcentaje / 100.0)
        val total = precio - desc

        valorDescuento = desc
        totalPagar = total
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

        // campo para el precio
        CampoNumerico(
            etiqueta = R.string.precio_producto,
            valor = precioInput,
            onValueChange = { precioInput = it },
            imeAction = ImeAction.Next,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // campo para el porcentaje de descuento
        CampoNumerico(
            etiqueta = R.string.porcentaje_descuento,
            valor = descuentoInput,
            onValueChange = { descuentoInput = it },
            imeAction = ImeAction.Done,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // mostrar error si hay
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.Start)
            )
        }

        // botones calcular y limpiar
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

        // tarjeta de resultados
        if (valorDescuento != null && totalPagar != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.resultado_titulo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    FilaResultado(stringResource(R.string.valor_descuento), formatearMoneda(valorDescuento!!))
                    Spacer(modifier = Modifier.height(8.dp))
                    FilaResultado(stringResource(R.string.total_pagar), formatearMoneda(totalPagar!!), isBold = true)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // mi firma
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
    imeAction: ImeAction,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(stringResource(etiqueta)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction
        ),
        modifier = modifier
    )
}

@Composable
fun FilaResultado(concepto: String, valor: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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

fun formatearMoneda(valor: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }
    return format.format(valor)
}

@Preview(showBackground = true)
@Composable
fun CalculadoraDescuentoPreview() {
    CalculadoraDescuentoTheme {
        CalculadoraDescuentoApp()
    }
}
