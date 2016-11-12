package uk.ac.soton.ecs.rdcn1g14.hybridimage;

import java.io.File;
import java.io.IOException;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.convolution.Gaussian2D;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;

public class MyConvolution implements SinglebandImageProcessor<Float, FImage> {
	
	private float[][] kernel;

	public MyConvolution(float[][] kernel) {
		this.kernel = kernel;
	}

	@Override
	public void processImage(FImage image) {

		FImage imageTemp = image.clone();

	  	// half of kernel's length
	  	int halfx = (kernel.length-1)/2;
	  	int halfy = (kernel[0].length-1)/2;
	  	
	  	// System.out.println("height " + kernel.length + " width " + kernel[0].length);
	  	
		// convolve image with kernel and store result back in image
		for(int i = 0; i < image.getHeight(); i++) {
			for(int j = 0; j < image.getWidth(); j++) {
				
				// if pixel at the edge
				if(i < halfx || i >= image.height - halfx || j < halfy || j >= image.width - halfy) {
					// set pixel to black
					imageTemp.pixels[i][j] = 0;
					// System.out.println("pixels[" + i + "][" + j + "]=" + 0f);	
					
				} else {
					float sum = 0f;
							
					for(int x = 0; x < kernel.length; x++) {
						for(int y = 0; y < kernel.length; y++) {
							// find out coordinates of image pixel in each kernel cell
							int ni = i - halfx + x;
							int nj = j - halfy + y;
							// every kernel weight x corresponding pixel value
							sum = sum + kernel[x][y] * image.pixels[ni][nj];
						}
					}
					
					// 3 x 3 kernel
					/*sum = sum + kernel[0][0]*image.pixels[i-1][j-1] + kernel[1][0]*image.pixels[i][j-1] + kernel[2][0]*image.pixels[i+1][j-1]
							 + kernel[0][1]*image.pixels[i-1][j] + kernel[1][1]*image.pixels[i][j] + kernel[2][1]*image.pixels[i+1][j]
							+ kernel[0][2]*image.pixels[i-1][j+1] + kernel[1][2]*image.pixels[i][j+1] + kernel[2][2]*image.pixels[i+1][j+1];
					*/
					
					imageTemp.pixels[i][j] = sum;
				}
			}
		}
		imageTemp = imageTemp.normalise();
		image.internalAssign(imageTemp);
	}
	
	public static void main( String[] args ) throws IOException {
		
		// System.out.println("Working Directory = " + System.getProperty("user.dir"));
		
		// set square kernel
	  	/*float[][] kernel = { {1, 1, 1, 1, 1, 1, 1},
	  						 {1, 1, 1, 1, 1, 1, 1},
	  						 {1, 1, 1, 1, 1, 1, 1},
	  						 {1, 1, 1, 2, 1, 1, 1},
	  						 {1, 1, 1, 1, 1, 1, 1},
	  						 {1, 1, 1, 1, 1, 1, 1},
	  						 {1, 1, 1, 1, 1, 1, 1}
	           			   };*/
	  	
		// Low-pass Convolution
	  	// set Gaussian kernel
	  	int sigma = 4;
	  	// (this implies the window is +/- 4 sigmas from the centre of the Gaussian)
	  	int size = (int) (8.0f * sigma + 1.0f); 
	  	if (size % 2 == 0) 
	  		// size must be odd
	  		size++; 
	  	
	  	float[][] kernel = Gaussian2D.createKernelImage(size, sigma).pixels;
	  	
		// load and display images
		MBFImage image = ImageUtilities.readMBF(new File("./data/dog.bmp"));
	  	//DisplayUtilities.display(dog, "Original Dog");
	  	
	  	MBFImage dog = image.process(new MyConvolution(kernel));
	  	DisplayUtilities.display(dog, "Low-passed Dog");
	  	
	  	
	  	// High-pass Convolution
	  	// set Gaussian kernel
	  	sigma = 8;
	  	// (this implies the window is +/- 4 sigmas from the centre of the Gaussian)
	  	size = (int) (8.0f * sigma + 1.0f); 
	  	if (size % 2 == 0) 
	  		// size must be odd
	  		size++; 
	  	
	  	kernel = Gaussian2D.createKernelImage(size, sigma).pixels;
	  	
		image = ImageUtilities.readMBF(new File("./data/cat.bmp"));
	  	//DisplayUtilities.display(cat, "Original Cat");
	  	
	  	MBFImage cat = image.process(new MyConvolution(kernel));
	  	// subtract original cat by low-passed cat
	  	cat = image.subtract(cat);
	  	// add 0.5 to every pixel for visualisation
	  	cat.addInplace(0.5f);
	  	DisplayUtilities.display(cat, "High-passed Cat");
	  	
	  	// add both images together
	  	image = dog.add(cat);
	  	image.subtractInplace(0.5f);
        DisplayUtilities.display(image, "Hybrid Image");
        image = ResizeProcessor.halfSize(image);
        DisplayUtilities.display(image, "Half size Image");
        image = ResizeProcessor.halfSize(image);
        DisplayUtilities.display(image, "Quarter size Image");
	}
}