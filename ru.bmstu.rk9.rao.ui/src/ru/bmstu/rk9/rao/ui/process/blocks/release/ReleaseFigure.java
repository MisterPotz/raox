package ru.bmstu.rk9.rao.ui.process.blocks.release;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Path;

import ru.bmstu.rk9.rao.ui.process.blocks.BlockFigure;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionAnchor;

public class ReleaseFigure extends BlockFigure {

	static class Shape extends Figure {

		@Override
		final protected void paintFigure(Graphics graphics) {
			Path path = new Path(null);
			Point shift = addArcToPath(getBounds(), path);
			final float[] points = path.getPathData().points;
			final float xStart = points[0];
			final float yStart = points[1];
			path.lineTo(xStart, yStart);
			path.close();

			graphics.setBackgroundColor(getForegroundColor());
			graphics.translate(shift.x, shift.y);
			graphics.fillPath(path);
			graphics.translate(-shift.x, -shift.y);
		}

		private static IFigure create() {
			return new Shape();
		}
	}

	public ReleaseFigure() {
		super(Shape.create());

		ConnectionAnchor inputConnectionAnchor = new ConnectionAnchor(getShape());
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(ReleaseNode.DOCK_IN, inputConnectionAnchor);

		ConnectionAnchor outputConnectionAnchor = new ConnectionAnchor(getShape());
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(ReleaseNode.DOCK_OUT, outputConnectionAnchor);

		getShape().addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure shape) {
				Path path = new Path(null);
				Point shift = addArcToPath(getShape().getBounds(), path);
				path.close();
				if (path.getPathData().points.length == 0)
					return;
				final int left = (int) path.getPathData().points[0];

				Rectangle bounds = shape.getBounds();
				inputConnectionAnchor.setOffsetHorizontal(shift.x + left - bounds.x);
				inputConnectionAnchor.setOffsetVertical(shift.y + bounds.height / 2);

				outputConnectionAnchor.setOffsetHorizontal(shift.x + bounds.width);
				outputConnectionAnchor.setOffsetVertical(shift.y + bounds.height / 2);
			}
		});
	}

	private static Point addArcToPath(Rectangle bounds, Path path) {
		final int startAngle = 240;
		final int arcAngle = 240;
		path.addArc(bounds.x, bounds.y, bounds.width, bounds.height, startAngle, arcAngle);

		final Point shift;
		if (bounds.width == 0) {
			shift = new Point(0, 0);
		} else {
			final float xStart = path.getPathData().points[0];
			shift = new Point((int) ((bounds.x - xStart) / 2), 0);
		}
		return shift;
	}
}
