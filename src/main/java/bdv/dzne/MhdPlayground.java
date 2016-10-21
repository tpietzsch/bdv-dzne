package bdv.dzne;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;
import ij.plugin.PlugIn;
import net.imagej.patcher.LegacyEnvironment;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;

public class MhdPlayground implements PlugIn
{
	public static void main( final String[] args ) throws ClassNotFoundException
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		System.setProperty( "ij.dir", "/Users/Pietzsch/Desktop/Fiji.app" );
		System.setProperty( "fiji.dir", "/Users/Pietzsch/Desktop/Fiji.app" );
		System.setProperty( "imagej.dir", "/Users/Pietzsch/Desktop/Fiji.app" );
		System.setProperty( "ij1.plugin.dirs", "/Users/Pietzsch/Desktop/Fiji.app/plugins" );

		final LegacyEnvironment ij1 = new LegacyEnvironment( null, false );
		ij1.addPluginClasspath( Thread.currentThread().getContextClassLoader() );
		ij1.main();
		ij1.runPlugIn( "bdv.dzne.MhdPlayground", null );
	}

	@Override
	public void run( final String arg )
	{
		try
		{
			doit();
		}
		catch ( final IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class ImpInfo
	{
		ImagePlus imp;

		PartiallyParsedMhd header;

		final AffineTransform3D sourceTransform = new AffineTransform3D();

		BdvStackSource< ? > bdvSource;

		ImpInfo( final ImagePlus imp )
		{
			this.imp = imp;
		}

		void loadHeader() throws IOException
		{
			final FileInfo info = imp.getOriginalFileInfo();
			String impfn = info.directory + "/" + info.fileName;
			if ( impfn.endsWith( ".raw" ) )
				impfn = impfn.substring( 0, impfn.length() - 4 ) + ".mhd";
			final BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream( impfn ) ) );
			header = new PartiallyParsedMhd( in );
			in.close();
		}

		void showInBdv( final Bdv bdv )
		{
			final FileInfo info = imp.getOriginalFileInfo();
			final Img< ? > img = ImageJFunctions.wrap( imp );
			header.getSourceTransform( sourceTransform );
			bdvSource = BdvFunctions.show( img, info.fileName, Bdv.options().sourceTransform( sourceTransform ).addTo( bdv ) );
		}
	}

	private ImpInfo input;

	public void doit() throws IOException
	{
		final String fnInput = "/Users/Pietzsch/Desktop/data/Christopher/Results_prototype/C2-95A.mhd";

		IJ.open( fnInput );

		final ImagePlus imp = IJ.getImage();

		input = new ImpInfo( imp );
		input.loadHeader();
		input.showInBdv( null );
		input.bdvSource.setColor( new ARGBType( 0x00ff00ff ) );
		input.bdvSource.setCurrent();
	}
}
