package com.sebastianllano.calculadoranomina

import java.text.NumberFormat
import java.util.Locale

// valores base de la nomina por la norma
const val SMMLV_2026 = 1750905.0

// auxilio de transporte para este año
const val AUXILIO_TRANSPORTE_2026 = 249095.0

// horas al mes con la jornada de 42 horas
const val HORAS_ORDINARIAS_MENSUALES = 210.0

// aporte de salud que paga el empleado
const val PORCENTAJE_SALUD = 0.04

// aporte de pension
const val PORCENTAJE_PENSION = 0.04

// fondo de solidaridad si ganas mas de 4 minimos
const val PORCENTAJE_FONDO_SOLIDARIDAD = 0.01

enum class RangoSalarial {
    RANGO_1,
    RANGO_2,
    RANGO_3
}

data class ResultadoNomina(
    val valorHora: Double,
    val salarioBasico: Double,
    val totalHorasExtra: Double,
    val auxilioTransporte: Double,
    val totalDevengado: Double,
    val aporteSalud: Double,
    val aportePension: Double,
    val fondoSolidaridad: Double,
    val totalDeducciones: Double,
    val salarioNeto: Double,
    val rango: RangoSalarial,
    val equivalenciaSmmv: Double
)

fun clasificarRango(salarioBasico: Double): RangoSalarial {
    return when {
        salarioBasico <= 2 * SMMLV_2026 -> RangoSalarial.RANGO_1
        salarioBasico < 4 * SMMLV_2026 -> RangoSalarial.RANGO_2
        else -> RangoSalarial.RANGO_3
    }
}

fun calcularNomina(
    salarioBasico: Double,
    horasDiurnas: Double,
    horasNocturnas: Double,
    esDominical: Boolean,
    transporteEmpresa: Boolean
): ResultadoNomina {
    // saco el valor de la hora normal
    val valorHora = salarioBasico / HORAS_ORDINARIAS_MENSUALES

    // recargo de las horas extra si es dominical o normal
    val factorDiurno = if (esDominical) 2.15 else 1.25
    val factorNocturno = if (esDominical) 2.65 else 1.75

    val pagoExtrasDiurnas = horasDiurnas * valorHora * factorDiurno
    val pagoExtrasNocturnas = horasNocturnas * valorHora * factorNocturno
    val totalHorasExtra = pagoExtrasDiurnas + pagoExtrasNocturnas

    // calculo el ibc sumando el basico y las horas extra
    val ibc = salarioBasico + totalHorasExtra

    // miro si aplica el auxilio de transporte
    val auxilioTransporte = if (salarioBasico <= 2 * SMMLV_2026 && !transporteEmpresa) {
        AUXILIO_TRANSPORTE_2026
    } else {
        0.0
    }

    // total devengado sumando todo
    val totalDevengado = ibc + auxilioTransporte

    // deducciones de salud y pension
    val aporteSalud = ibc * PORCENTAJE_SALUD
    val aportePension = ibc * PORCENTAJE_PENSION
    val fondoSolidaridad = if (ibc >= 4 * SMMLV_2026) {
        ibc * PORCENTAJE_FONDO_SOLIDARIDAD
    } else {
        0.0
    }
    val totalDeducciones = aporteSalud + aportePension + fondoSolidaridad

    // lo que le queda al empleado neto
    val salarioNeto = totalDevengado - totalDeducciones

    // clasificar en que rango esta el salario
    val rango = clasificarRango(salarioBasico)

    // cuantos salarios minimos se gana mas o menos
    val equivalenciaSmmv = salarioNeto / SMMLV_2026

    return ResultadoNomina(
        valorHora = valorHora,
        salarioBasico = salarioBasico,
        totalHorasExtra = totalHorasExtra,
        auxilioTransporte = auxilioTransporte,
        totalDevengado = totalDevengado,
        aporteSalud = aporteSalud,
        aportePension = aportePension,
        fondoSolidaridad = fondoSolidaridad,
        totalDeducciones = totalDeducciones,
        salarioNeto = salarioNeto,
        rango = rango,
        equivalenciaSmmv = equivalenciaSmmv
    )
}

fun formatearMoneda(valor: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }
    return format.format(valor)
}
