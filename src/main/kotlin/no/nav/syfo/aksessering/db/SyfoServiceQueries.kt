package no.nav.syfo.aksessering.db

import java.io.StringReader
import java.sql.ResultSet
import java.time.LocalDateTime
import no.nav.helse.sm2013.HelseOpplysningerArbeidsuforhet
import no.nav.syfo.db.DatabaseInterface
import no.nav.syfo.db.toList
import no.nav.syfo.model.ReceivedSykmelding
import no.nav.syfo.model.toSykmelding
import no.nav.syfo.utils.fellesformatUnmarshaller

fun DatabaseInterface.hentSykmeldinger(): List<ReceivedSykmelding> =
        connection.use { connection ->
            connection.prepareStatement(
                    """
                        SELECT * FROM (
                            SELECT syk.*, row_number() over (ORDER BY created ASC) line_number
                            FROM SYKMELDING_DOK syk
                            ) 
                        WHERE line_number BETWEEN 0 AND 10 ORDER BY line_number
                        """
            ).use {
                it.executeQuery().toList { toReceivedSykmelding() }
            }
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
