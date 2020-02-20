/*
* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
*   - Redistributions of source code must retain the above copyright
*     notice, this list of conditions and the following disclaimer.
*
*   - Redistributions in binary form must reproduce the above copyright
*     notice, this list of conditions and the following disclaimer in the
*     documentation and/or other materials provided with the distribution.
*
*   - Neither the name of Oracle or the names of its
*     contributors may be used to endorse or promote products derived
*     from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
* IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
* THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
* PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.JComponent;

import javax.swing.plaf.LayerUI;

class BlurLayerUI extends LayerUI<Container>
{
	private BufferedImage mOffscreenImage;
	private BufferedImageOp mOperation;

	public BlurLayerUI()
	{
		float ninth = 1.0f / 9.0f;
		float[] blurKernel = {ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth};
		mOperation = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, null);
	}

	@Override
	public void paint (Graphics g, JComponent c)
	{
		int w = c.getWidth();
		int h = c.getHeight();

		if(w == 0 || h == 0)
		{
			return;
		}

		// Only create the offscreen image if the one we have is the wrong size.
		if(mOffscreenImage == null || mOffscreenImage.getWidth() != w || mOffscreenImage.getHeight() != h)
		{
			mOffscreenImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		}

		Graphics2D ig2 = mOffscreenImage.createGraphics();
		ig2.setClip(g.getClip());
		super.paint(ig2, c);
		ig2.dispose();

		Graphics2D g2 = (Graphics2D)g;
		g2.drawImage(mOffscreenImage, mOperation, 0, 0);

		// Gray it out.
		Composite urComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f * 1f));
		g2.fillRect(0, 0, w, h);
		g2.setComposite(urComposite);

		g2.dispose();
	}
}
