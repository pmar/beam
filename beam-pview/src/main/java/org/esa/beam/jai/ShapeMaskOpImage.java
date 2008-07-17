package org.esa.beam.jai;

import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;

/**
 * Creates a mask image for a given {@link org.esa.beam.framework.datamodel.RasterDataNode}.
 * The resulting image will have a single-band, interleaved sample model
 * with sample values 255 or 0.
 */
public class ShapeMaskOpImage extends SingleBandedOpImage {
    private static final byte FALSE = (byte) 0;
    private static final byte TRUE = (byte) 255;
    private final Shape shape;
    private final ColorModel colorModel;

    public ShapeMaskOpImage(Shape shape, int sourceWidth, int sourceHeight, int level) {
        super(DataBuffer.TYPE_BYTE, sourceWidth, sourceHeight, null, null, level, null);
        this.shape = AffineTransform.getScaleInstance(getScale(), getScale()).createTransformedShape(shape);
        this.colorModel = PlanarImage.createColorModel(getSampleModel());
    }

    protected LevelOpImage createDownscaledImage(int level) {
        return new ShapeMaskOpImage(shape, getSourceWidth(), getSourceHeight(), level);
    }

    @Override
    protected void computeRect(PlanarImage[] sourceImages, WritableRaster tile, Rectangle destRect) {
        final BufferedImage image = new BufferedImage(colorModel, RasterFactory.createWritableRaster(tile.getSampleModel(), tile.getDataBuffer(), new Point(0, 0)), false, null);
        final Graphics2D graphics2D = image.createGraphics();
        graphics2D.translate(-tile.getMinX(), -tile.getMinY());
        graphics2D.setColor(Color.WHITE);
        graphics2D.fill(shape);
        graphics2D.dispose();

        final byte[] data = ((DataBufferByte) tile.getDataBuffer()).getData();
        for (int i = 0; i < data.length; i++) {
            data[i] = (data[i] != 0) ? TRUE : FALSE;
        }
    }
}