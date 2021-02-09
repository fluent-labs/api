import com.foreignlanguagereader.jobs.definitions.{
  TemplateExtractor,
  WiktionaryGenericText
}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{Dataset, SparkSession}
import org.scalatest.funspec.AnyFunSpec

class TemplateExtractorTest extends AnyFunSpec {
  implicit val spark: SparkSession = {
    SparkSession
      .builder()
      .master("local")
      .appName("TemplateExtractorTest")
      .getOrCreate()
  }
  import spark.implicits._

  val text: String =
    """===Pronunciation===
      |* {{a|RP}} {{IPA|en|/ˈdɪkʃ(ə)n(ə)ɹi/}}
      |* {{a|GenAm|Canada}} {{enPR|dĭk'shə-nĕr-ē}}, {{IPA|en|/ˈdɪkʃəˌnɛɹi/}}
      |* {{audio|en|en-us-dictionary.ogg|Audio (US, California)}}
      |* {{audio|en|en-uk-dictionary.ogg|Audio (UK)}}
      |* {{hyphenation|en|dic|tion|ary}}
      |* {{rhymes|en|ɪkʃənɛəɹi}}""".stripMargin

  val entryraw: WiktionaryGenericText = WiktionaryGenericText(text)
  val data: Dataset[WiktionaryGenericText] = Seq(entryraw).toDS()

  describe("it can extract templates from an entry") {
    it("can get all instances of templates with their arguments") {
      val instances =
        TemplateExtractor.extractTemplateInstances(data).cache()
      assert(instances.count() == 9L)
    }

    it("can count how many times a template was used") {
      val instances = TemplateExtractor.extractTemplateInstances(data)
      val counts = TemplateExtractor.extractTemplateCount(instances).cache()

      assert(counts.count() == 6L)
      assert(counts.filter(col("count") > 1).count() == 3L)
    }
  }
}
