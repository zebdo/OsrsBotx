package net.runelite.client.plugins.mousetrail;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


public enum Theme
{
    RAINBOW,
    COLOR_TEMP(
			new Color(195, 209, 255),
			new Color(195, 210, 255),
			new Color(196, 210, 255),
			new Color(197, 210, 255),
			new Color(197, 211, 255),
			new Color(197, 211, 255),
			new Color(198, 212, 255),
			new Color(198, 212, 255),
			new Color(199, 212, 255),
			new Color(200, 213, 255),
			new Color(200, 213, 255),
			new Color(201, 214, 255),
			new Color(202, 214, 255),
			new Color(202, 215, 255),
			new Color(203, 215, 255),
			new Color(204, 216, 255),
			new Color(204, 216, 255),
			new Color(205, 217, 255),
			new Color(206, 217, 255),
			new Color(207, 218, 255),
			new Color(207, 218, 255),
			new Color(205, 220, 255),
			new Color(206, 220, 255),
			new Color(207, 221, 255),
			new Color(207, 221, 255),
			new Color(208, 222, 255),
			new Color(209, 223, 255),
			new Color(210, 223, 255),
			new Color(211, 224, 255),
			new Color(212, 225, 255),
			new Color(214, 225, 255),
			new Color(215, 226, 255),
			new Color(216, 227, 255),
			new Color(217, 227, 255),
			new Color(218, 229, 255),
			new Color(220, 229, 255),
			new Color(221, 230, 255),
			new Color(222, 230, 255),
			new Color(224, 231, 255),
			new Color(225, 232, 255),
			new Color(227, 233, 255),
			new Color(228, 234, 255),
			new Color(230, 235, 255),
			new Color(231, 236, 255),
			new Color(233, 237, 255),
			new Color(235, 238, 255),
			new Color(237, 239, 255),
			new Color(239, 240, 255),
			new Color(240, 241, 255),
			new Color(243, 242, 255),
			new Color(245, 243, 255),
			new Color(247, 245, 255),
			new Color(249, 246, 255),
			new Color(252, 247, 255),
			new Color(254, 249, 255),
			new Color(255, 249, 253),
			new Color(255, 248, 251),
			new Color(255, 246, 247),
			new Color(255, 245, 245),
			new Color(255, 244, 242),
			new Color(255, 243, 239),
			new Color(255, 242, 236),
			new Color(255, 240, 233),
			new Color(255, 239, 230),
			new Color(255, 238, 227),
			new Color(255, 236, 224),
			new Color(255, 235, 220),
			new Color(255, 233, 217),
			new Color(255, 232, 213),
			new Color(255, 230, 210),
			new Color(255, 228, 206),
			new Color(255, 227, 202),
			new Color(255, 225, 198),
			new Color(255, 223, 194),
			new Color(255, 221, 190),
			new Color(255, 219, 186),
			new Color(255, 217, 182),
			new Color(255, 215, 177),
			new Color(255, 213, 173),
			new Color(255, 211, 168),
			new Color(255, 209, 163),
			new Color(255, 206, 159),
			new Color(255, 204, 153),
			new Color(255, 201, 148),
			new Color(255, 199, 143),
			new Color(255, 196, 137),
			new Color(255, 193, 132),
			new Color(255, 190, 126),
			new Color(255, 187, 120),
			new Color(255, 184, 114),
			new Color(255, 180, 107),
			new Color(255, 177, 101),
			new Color(255, 173, 94),
			new Color(255, 169, 87),
			new Color(255, 165, 79),
			new Color(255, 161, 72),
			new Color(255, 157, 63),
			new Color(255, 152, 54),
			new Color(255, 147, 44),
			new Color(255, 142, 33),
			new Color(255, 138, 18),
			new Color(255, 131, 0),
			new Color(255, 126, 0),
			new Color(255, 121, 0),
			new Color(255, 115, 0),
			new Color(255, 109, 0),
			new Color(255, 101, 0),
			new Color(255, 93, 0),
			new Color(255, 83, 0),
			new Color(255, 71, 0),
			new Color(255, 56, 0)
	);

	private final List<PerceptualGradient> gradients;

	Theme()
	{
		gradients = new ArrayList<>();
	}

	Theme(Color... colors) {
		gradients = new ArrayList<>();
		for (int i=0; i<colors.length; i++) {
			Color startColor = colors[i];
			// When at the last index then wrap back to first color
			Color endColor = i == colors.length-1 ? colors[0] : colors[i+1];
            gradients.add(new PerceptualGradient(startColor, endColor));
        }
    }

    public Color getColor(float ratio)
    {
        // Subtract floor to match Color.getHSBColor() functionality
        ratio = Math.abs((float) (ratio - Math.floor(ratio)));

        if (gradients.size() == 0) { // This applies to the RAINBOW theme only.
            return Color.getHSBColor(ratio, 1.0f, 1.0f);
        }

        float increment = 1.0f/gradients.size(); // Since size isn't 0, increment is always (0-1]
        PerceptualGradient gradient = gradients.get((int) Math.floor(ratio/increment));
        float relativeRatio = (ratio%increment)/increment;
        return gradient.getColorMix(relativeRatio);
    }
}
