package no.nav.syfo.aksessering.db

import java.io.StringReader
import java.sql.ResultSet
import java.time.LocalDateTime
import no.nav.helse.sm2013.HelseOpplysningerArbeidsuforhet
import no.nav.syfo.db.DatabaseInterface
import no.nav.syfo.db.toList
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.toSykmelding
import no.nav.syfo.objectMapper
import no.nav.syfo.utils.fellesformatUnmarshaller

fun DatabaseInterface.hentSykmeldinger(startlinje: Int, stoplinje: Int): List<String> =
    connection.use { connection ->
        connection.prepareStatement(
            """
                        SELECT * FROM (
                            SELECT syk.*, row_number() over (ORDER BY created ASC) line_number
                            FROM SYKMELDING_DOK syk
                            WHERE created < to_timestamp('2019-11-04','YYYY-MM-DD')
                            ) 
                        WHERE line_number BETWEEN ? AND ? ORDER BY line_number
                        """
        ).use {
            it.setInt(1, startlinje)
            it.setInt(2, stoplinje)
            it.executeQuery().toJsonString()
        }
    }

fun ResultSet.toJsonString(): List<String> {

    val listOfRows = ArrayList<String>()

    val metadata = this.metaData
    val columns = metadata.columnCount

    while (this.next()) {
        val rowMap = HashMap<String, Any?>()
        for (i in 1..columns) {

            var data: Any?
            if (metadata.getColumnClassName(i).contains("oracle.sql.TIMESTAMP")) {
                data = getTimestamp(i)
            } else if (metadata.getColumnClassName(i).contains("oracle.sql.CLOB") || metadata.getColumnName(i) == "DOKUMENT") {
                data = getString(i)
            } else {
                data = getObject(i)
            }
            rowMap[metadata.getColumnName(i)] = data
        }
        listOfRows.add(objectMapper.writeValueAsString(rowMap))
    }
    return listOfRows.map { objectMapper.writeValueAsString(it) }
}

fun ResultSet.toReceivedSykmelding(): ReceivedSykmelding =
    ReceivedSykmelding(
        sykmelding = unmarshallerToHealthInformation(getString("dokument")).toSykmelding(
            sykmeldingId = getString("melding_id"),
            pasientAktoerId = getString("aktor_id"),
            legeAktoerId = "",
            msgId = "",
            signaturDato = getTimestamp("created").toLocalDateTime()
        ),
        personNrPasient = unmarshallerToHealthInformation(getString("dokument")).pasient.fodselsnummer.id,
        tlfPasient = unmarshallerToHealthInformation(getString("dokument")).pasient.kontaktInfo.firstOrNull()?.teleAddress?.v,
        personNrLege = "",
        navLogId = getString("mottak_id"),
        msgId = "",
        legekontorOrgNr = "",
        legekontorOrgName = "",
        legekontorHerId = "",
        legekontorReshId = "",
        mottattDato = LocalDateTime.now(),
        rulesetVersion = unmarshallerToHealthInformation(getString("dokument")).regelSettVersjon,
        fellesformat = "",
        tssid = ""
    )

fun unmarshallerToHealthInformation(healthInformation: String): HelseOpplysningerArbeidsuforhet =
    fellesformatUnmarshaller.unmarshal(StringReader(healthInformation)) as HelseOpplysningerArbeidsuforhet

fun DatabaseInterface.hentAntallSykmeldinger(): List<AntallSykmeldinger> =
    connection.use { connection ->
        connection.prepareStatement(
            """
                        SELECT COUNT(MOTTAK_ID) AS antall
                        FROM SYKMELDING_DOK
                        WHERE created < to_timestamp('2019-11-04','YYYY-MM-DD')
                        """
        ).use {
            it.executeQuery().toList { toAntallSykmeldinger() }
        }
    }

data class AntallSykmeldinger(
    val antall: String
)

fun ResultSet.toAntallSykmeldinger(): AntallSykmeldinger =
    AntallSykmeldinger(
        antall = getString("antall")
    )
