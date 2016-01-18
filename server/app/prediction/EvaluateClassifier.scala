package prediction

import java.io.{BufferedWriter, File => JFile, FileWriter}

import nak.NakContext._
import nak.core._
import nak.data.Example
import nak.util.ConfusionMatrix

/**
 * Created by erna on 10/27/15.
 */
object EvaluateClassifier extends App {

  def run = {
    val evalDir = new JFile("test")
    val classifier = loadClassifier[IndexedClassifier[String] with FeaturizedClassifier[String, String]]("classifier")
    // Evaluate
    println("Evaluating...")
    val bw: BufferedWriter = new BufferedWriter(new FileWriter(new JFile("testergebnis.txt")))
    val labels = maxLabel(classifier.labels) _
    val comparisons = for (ex: Example[String, String] <- fromLabeledDirs(evalDir).toList) yield {
      bw.write(ex.label + " " + classifier.predict(ex.features))
      bw.newLine()
      (ex.label, labels(classifier.evalRaw(ex.features)), ex.features)
    }

    bw.close()
    val (goldLabels, predictions, inputs) = comparisons.unzip3
    println(ConfusionMatrix(goldLabels, predictions, inputs))
  }
  run
}
