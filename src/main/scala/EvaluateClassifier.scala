import java.io.{FileWriter, BufferedWriter, File ⇒ JFile}

import nak.NakContext._
import nak.core.{FeaturizedClassifier, IndexedClassifier}
import nak.util.ConfusionMatrix

/**
 * Created by erna on 10/27/15.
 */
object EvaluateClassifier extends App {

  val evalDir = new JFile("test")
  val classifier = loadClassifier[IndexedClassifier[String] with FeaturizedClassifier[String, String]]("classifier")
  // Evaluate
  println("Evaluating...")
  val bw: BufferedWriter = new BufferedWriter(new FileWriter(new JFile("testergebnis.txt")))
  val labels = maxLabel(classifier.labels) _
  val comparisons = for (ex <- fromLabeledDirs(evalDir).toList) yield {
    bw.write(ex.label + ", " + classifier.predict(ex.features))
    bw.newLine()
    (ex.label, labels(classifier.evalRaw(ex.features)), ex.features)
  }

  bw.close()
  val (goldLabels, predictions, inputs) = comparisons.unzip3
  println(ConfusionMatrix(goldLabels, predictions, inputs))

}
