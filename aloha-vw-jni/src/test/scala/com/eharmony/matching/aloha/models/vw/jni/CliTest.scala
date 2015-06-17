package com.eharmony.matching.aloha.models.vw.jni

import com.eharmony.matching.testhelp.io.{IoCaptureCompanion, TestWithIoCapture}
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.BlockJUnit4ClassRunner
import spray.json.{DeserializationException, pimpString}

object CliTest extends IoCaptureCompanion

@RunWith(classOf[BlockJUnit4ClassRunner])
class CliTest extends TestWithIoCapture(CliTest) {
    import CliTest._

    @Test def testMissingBothParams(): Unit = {
        Cli.main(Array.empty)
        val expected =
            """
              |Error: Missing option --spec
              |Error: Missing option --model
              |vw 1.x
              |Usage: vw [options]
              |
              |  -s <value> | --spec <value>
              |        spec is an Apache VFS URL to an aloha spec file with modelType 'VwJNI'.
              |  -m <value> | --model <value>
              |        model is an Apache VFS URL to a VW binary model.
            """.stripMargin
        assertEquals(expected.trim, errContent.trim)
    }

    @Test def testMissingSpecParam(): Unit = {
        Cli.main(Array("-m", "res:com/eharmony/matching/aloha/models/vw/jni/logistic.test.model"))
        val expected =
            """
              |Error: Missing option --spec
              |vw 1.x
              |Usage: vw [options]
              |
              |  -s <value> | --spec <value>
              |        spec is an Apache VFS URL to an aloha spec file with modelType 'VwJNI'.
              |  -m <value> | --model <value>
              |        model is an Apache VFS URL to a VW binary model.
            """.stripMargin
        assertEquals(expected.trim, errContent.trim)
    }

    @Test def testMissingModelParam(): Unit = {
        Cli.main(Array("-s", "res:com/eharmony/matching/aloha/models/vw/jni/good.logistic.aloha.js"))
        val expected =
            """
              |Error: Missing option --model
              |vw 1.x
              |Usage: vw [options]
              |
              |  -s <value> | --spec <value>
              |        spec is an Apache VFS URL to an aloha spec file with modelType 'VwJNI'.
              |  -m <value> | --model <value>
              |        model is an Apache VFS URL to a VW binary model.
              |
            """.stripMargin
        assertEquals(expected.trim, errContent.trim)
    }

    @Test def testSpecFileDoesntExist(): Unit = {
        Cli.main(Array("-m", "res:com/eharmony/matching/aloha/models/vw/jni/logistic.test.model", "-s", "res:SPECTHATDOESNTEXIST"))
        val expected =
            """
              |Error: Option --spec failed when given 'res:SPECTHATDOESNTEXIST'. Badly formed URI "res:SPECTHATDOESNTEXIST".
              |vw 1.x
              |Usage: vw [options]
              |
              |  -s <value> | --spec <value>
              |        spec is an Apache VFS URL to an aloha spec file with modelType 'VwJNI'.
              |  -m <value> | --model <value>
              |        model is an Apache VFS URL to a VW binary model.
              |
            """.stripMargin
        assertEquals(expected.trim, errContent.trim)
    }

    @Test def testModelFileDoesntExist(): Unit = {
        Cli.main(Array("-m", "res:SPECTHATDOESNTEXIST", "-s", "res:com/eharmony/matching/aloha/models/vw/jni/good.logistic.aloha.js"))
        val expected =
            """
              |Error: Option --model failed when given 'res:SPECTHATDOESNTEXIST'. Badly formed URI "res:SPECTHATDOESNTEXIST".
              |vw 1.x
              |Usage: vw [options]
              |
              |  -s <value> | --spec <value>
              |        spec is an Apache VFS URL to an aloha spec file with modelType 'VwJNI'.
              |  -m <value> | --model <value>
              |        model is an Apache VFS URL to a VW binary model.
              |
            """.stripMargin
        assertEquals(expected.trim, errContent.trim)
    }

    @Test def testArrayJson(): Unit = {
        try {
            Cli.main(Array("-m", "res:com/eharmony/matching/aloha/models/vw/jni/logistic.test.model",
                           "-s", "res:com/eharmony/matching/aloha/models/vw/jni/array.js"))
        }
        catch {
            case e: DeserializationException if e.getMessage == "Expected JSON object." =>
            case e: Throwable => throw e
        }
    }

    @Test def testModelAlreadyInJson(): Unit = {
        try {
            Cli.main(Array("-m", "res:com/eharmony/matching/aloha/models/vw/jni/logistic.test.model",
                           "-s", "res:com/eharmony/matching/aloha/models/vw/jni/withmodel.logistic.aloha.js"))
        }
        catch {
            case e: DeserializationException if e.getMessage == "JSON should not contain the path 'vw.model'." =>
            case e: Throwable => throw e
        }
    }

    @Test def testNoVwSectionInJson(): Unit = {
        try {
            Cli.main(Array("-m", "res:com/eharmony/matching/aloha/models/vw/jni/logistic.test.model",
                           "-s", "res:com/eharmony/matching/aloha/models/vw/jni/no.vw.aloha.js"))
        }
        catch {
            case e: DeserializationException if e.getMessage == "JSON does not contain a 'vw' object." =>
            case e: Throwable => throw e
        }
    }

    @Test def testHappy(): Unit = {
        Cli.main(Array("-m", "res:com/eharmony/matching/aloha/models/vw/jni/logistic.test.model",
                       "-s", "res:com/eharmony/matching/aloha/models/vw/jni/good.logistic.aloha.js"))

        val expected =
            """
              |{
              |  "modelType": "VwJNI",
              |  "modelId": { "id": 0, "name": "model name" },
              |  "features": {
              |    "height_mm": "Seq((\"1800\", 1.0))"
              |  },
              |  "namespaces": {
              |    "personal_features": [ "height_mm" ]
              |  },
              |  "vw": {
              |    "params": [
              |      "--quiet",
              |      "-t"
              |    ],
              |    "model": "BwAAADcuMTAuMABtAABIwgAASEISAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASAAAAIC0tbGluaz1sb2dpc3RpYyAAAFzFAQCohYk8"
              |  }
              |}
            """.stripMargin.parseJson

        assertEquals(expected, outContent.parseJson)
    }
}
