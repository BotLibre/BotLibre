package org.botlibre.test;

//import org.tensorflow.Graph;
//import org.tensorflow.Session;
//import org.tensorflow.Tensor;
//import org.tensorflow.TensorFlow;

/**
 * This tests that Tensorflow is installed correctly.
 * It can be tested using:
 * java -cp libtensorflow-1.6.0.jar:libtensorflow_jni-1.6.0.jar:. org.botlibre.test.TensorflowTest
 * java -cp libtensorflow-1.1.0.jar:libtensorflow_jni-1.1.0.jar:. org.botlibre.test.TensorflowTest
 * java -cp libtensorflow-1.6.0.jar:. -Djava.library.path=./jni org.botlibre.test.TensorflowTest
 */
public class TensorflowTest {
  public static void main(String[] args) throws Exception {
    /*try (Graph g = new Graph()) {
      final String value = "Hello from " + TensorFlow.version();

      // Construct the computation graph with a single operation, a constant
      // named "MyConst" with a value "value".
      try (Tensor t = Tensor.create(value.getBytes("UTF-8"))) {
        // The Java API doesn't yet include convenience functions for adding operations.
        g.opBuilder("Const", "MyConst").setAttr("dtype", t.dataType()).setAttr("value", t).build();
      }

      // Execute the "MyConst" operation in a Session.
      try (Session s = new Session(g);
           Tensor output = s.runner().fetch("MyConst").run().get(0)) {
        System.out.println(new String(output.bytesValue(), "UTF-8"));
      }
    }*/
  }
}