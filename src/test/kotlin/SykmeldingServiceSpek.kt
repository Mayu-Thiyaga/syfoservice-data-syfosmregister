import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import io.mockk.verify
import no.nav.syfo.aksessering.db.AntallSykmeldinger
import no.nav.syfo.aksessering.db.hentAntallSykmeldinger
import no.nav.syfo.aksessering.db.hentSykmeldinger
import no.nav.syfo.db.DatabaseInterface
import no.nav.syfo.kafka.SykmeldingKafkaProducer
import no.nav.syfo.service.SykmeldingService
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object SykmeldingServiceSpek : Spek({

    val database = mockkClass(DatabaseInterface::class)
    val sykmeldingKafkaProducer = mockkClass(SykmeldingKafkaProducer::class)

    beforeEachTest {
        clearAllMocks()
    }

    describe("Tester SykmeldingServiceSpek") {

        it("Skal hente ut alle sykmeldinger", timeout = 1000000000L) {

            every { sykmeldingKafkaProducer.publishToKafka(any()) } returns Unit
            mockkStatic("no.nav.syfo.aksessering.db.SyfoServiceQueriesKt")
            every { database.hentSykmeldinger(any(), any()) } returns 0.until(10).map { "" }
            every { database.hentSykmeldinger(21, 30) } returns 0.until(1).map { "" }
            every { database.hentAntallSykmeldinger() } returns listOf(AntallSykmeldinger("21"))
            val sykmeldingService = SykmeldingService(sykmeldingKafkaProducer, database, 10)
            sykmeldingService.run() shouldEqual 21
            verify(exactly = 1) { database.hentSykmeldinger(1, 10) }
            verify(exactly = 1) { database.hentSykmeldinger(11, 20) }
            verify(exactly = 1) { database.hentSykmeldinger(21, 30) }
            verify(exactly = 21) { sykmeldingKafkaProducer.publishToKafka(any()) }
        }

        it("Skal hente ut alle sykmeldinger 2") {
            every { sykmeldingKafkaProducer.publishToKafka(any()) } returns Unit
            mockkStatic("no.nav.syfo.aksessering.db.SyfoServiceQueriesKt")
            every { database.hentSykmeldinger(any(), any()) } returns 0.until(8).map { "" }
            every { database.hentAntallSykmeldinger() } returns listOf(AntallSykmeldinger("8"))
            val sykmeldingService = SykmeldingService(sykmeldingKafkaProducer, database, 10)
            sykmeldingService.run() shouldEqual 8
            verify(exactly = 1) { database.hentSykmeldinger(1, 10) }
            verify(exactly = 8) { sykmeldingKafkaProducer.publishToKafka(any()) }
        }
    }
})
