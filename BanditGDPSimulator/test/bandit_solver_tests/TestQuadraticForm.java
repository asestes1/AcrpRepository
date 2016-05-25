package bandit_solver_tests;

import java.io.File;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import bandit_objects.SimpleTmiAction;
import function_util.QuadraticFormFunction;
import function_util.QuadraticFormFunctionFactory;

public class TestQuadraticForm {
	private static final File testFile1 = new File("test/test_files/QuadForm2");

	@Test
	public void testFactory() throws Exception {
		QuadraticFormFunction myFunction = QuadraticFormFunctionFactory.parseQuadraticForm(testFile1);

		RealVector stateVector = new ArrayRealVector(new double[] { .7, .4 });
		SimpleTmiAction gdpAction = new SimpleTmiAction(50, 1500, 720, 360);
		System.out.println(myFunction.apply(stateVector, gdpAction));
	}

	@Test
	public void plotFunction() throws Exception {
		QuadraticFormFunction myFunction = QuadraticFormFunctionFactory.parseQuadraticForm(testFile1);
		//ReflectedBiFunction<RealVector, SimpleTmiAction> myFunction = new ReflectedBiFunction<RealVector, SimpleTmiAction>(
		//		myQuadFunction, 50.0);
		RealVector stateVector1 = new ArrayRealVector(new double[] { .25, .25 });
		RealVector stateVector2 = new ArrayRealVector(new double[] { .5, .5 });
		RealVector stateVector3 = new ArrayRealVector(new double[] { 1, 1 });

		String pts_X = "PTS_X = [";
		String pts_Y1 = "PTS_Y1=[";
		String pts_Y2 = "PTS_Y2=[";
		String pts_Y3 = "PTS_Y3 = [";

		int min = 0;
		int max = 60;
		int step = 1;
		for (int rate = min; rate <= max; rate += step) {
			pts_X += rate;
			// SimpleTmiAction gdpAction = new SimpleTmiAction(rate, 1500, 720,
			// 360);
			SimpleTmiAction gdpAction = new SimpleTmiAction(rate, 720, 360, 1500);

			pts_Y1 += myFunction.apply(stateVector1, gdpAction);
			pts_Y2 += myFunction.apply(stateVector2, gdpAction);
			pts_Y3 += myFunction.apply(stateVector3, gdpAction);

			if (rate < max) {
				pts_X += ",";
				pts_Y1 += ",";
				pts_Y2 += ",";
				pts_Y3 += ",";
			} else {
				pts_X += "];";
				pts_Y1 += "];";
				pts_Y2 += "];";
				pts_Y3 += "];";
			}
		}
		System.out.println(pts_X);
		System.out.println(pts_Y1);
		System.out.println(pts_Y2);
		System.out.println(pts_Y3);
		System.out.println("plot(PTS_X,PTS_Y1,'-r',PTS_X,PTS_Y2,'-b',PTS_X,PTS_Y3,'-k')");
	}

	@Test
	public void plotContour() throws Exception {
		QuadraticFormFunction myFunction = QuadraticFormFunctionFactory.parseQuadraticForm(testFile1);
		RealVector stateVector1 = new ArrayRealVector(new double[] { 0, 0 });
		RealVector stateVector2 = new ArrayRealVector(new double[] { .5, .5 });
		RealVector stateVector3 = new ArrayRealVector(new double[] { 1, 1 });

		String pts_X = "PTS_X = [";
		String pts_Y = "PTS_Y = [";
		@SuppressWarnings("unused")
		String pts_Z1 = "PTS_Z1=[";
		@SuppressWarnings("unused")
		String pts_Z2 = "PTS_Z2=[";
		String pts_Z3 = "PTS_Z3 = [";

		for (int rate = 0; rate <= 50; rate++) {
			pts_X += rate;

			if (rate < 50) {
				pts_X += ",";
			} else {
				pts_X += "];";
			}
		}

		for (int radius = 0; radius <= 720; radius += 20) {
			pts_Y += radius;

			if (radius < 720) {
				pts_Y += ",";
			} else {
				pts_Y += "];";
			}
		}

		for (int rate = 0; rate <= 50; rate++) {
			for (int radius = 0; radius <= 720; radius += 20) {
				SimpleTmiAction gdpAction = new SimpleTmiAction(rate, 720, radius, 1500);
				pts_Z1 += myFunction.apply(stateVector1, gdpAction);
				pts_Z2 += myFunction.apply(stateVector2, gdpAction);
				pts_Z3 += myFunction.apply(stateVector3, gdpAction);

				if (radius < 720) {
					pts_Z1 += ",";
					pts_Z2 += ",";
					pts_Z3 += ",";
				} else if (rate < 50) {
					pts_Z1 += ";\n";
					pts_Z2 += ";\n";
					pts_Z3 += ";\n";
				} else {
					pts_Z1 += "];";
					pts_Z2 += "];";
					pts_Z3 += "];";
				}
			}
		}
		System.out.println(pts_X);
		System.out.println(pts_Y);
		// System.out.println(pts_Z1);
		// System.out.println(pts_Z2);
		System.out.println(pts_Z3);
		// System.out.println("contour(PTS_X,PTS_Y,PTS_Z1)");
		System.out.println("figure()");
		// System.out.println("contour(PTS_X,PTS_Y,PTS_Z2)");
		System.out.println("figure()");
		System.out.println("contour(PTS_Y,PTS_X,PTS_Z3)");
	}
}
