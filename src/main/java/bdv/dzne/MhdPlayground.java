package bdv.dzne;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

import bdv.tools.transformation.TransformedSource;
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

	private Bdv bdv;

	private ImpInfo atlas;

	private ImpInfo input;

	private void saveManualRegistration()
	{
		System.out.println( "save manual registration" );

		final AffineTransform3D transform = getManualTransform( input.bdvSource ).preConcatenate( getManualTransform( atlas.bdvSource ).inverse() );
		input.header.preConcatenateTransform( transform );
		input.header.printModified();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
	}

	private AffineTransform3D getManualTransform( final BdvStackSource< ? > stackSource )
	{
		final TransformedSource< ? > source = ( TransformedSource< ? > ) stackSource.getSources().get( 0 ).getSpimSource();
		final AffineTransform3D manual = new AffineTransform3D();
		final AffineTransform3D fixedTransform = new AffineTransform3D();
		source.getIncrementalTransform( manual );
		source.getFixedTransform( fixedTransform );
		manual.concatenate( fixedTransform );
		return manual;
	}

	public void doit() throws IOException
	{
//		final String fnAtlas = "/Users/Pietzsch/Desktop/data/Christopher/Christopher Atlas/canon_T1_r(2).mha";
//		final String fnInput = "/Users/Pietzsch/Desktop/data/Christopher/C2-Fused-1_turned_mirrored.mhd";
		final String fnAtlas = "/Users/Pietzsch/Desktop/data/Christopher/Results_prototype/atlas.mhd";
		final String fnInput = "/Users/Pietzsch/Desktop/data/Christopher/Results_prototype/C2-95A.mhd";

		IJ.open( fnAtlas );
		atlas = new ImpInfo( IJ.getImage() );
		atlas.loadHeader();
		atlas.showInBdv( null );
		atlas.bdvSource.setColor( new ARGBType( 0x0000ff00 ) );
		bdv = atlas.bdvSource;

		IJ.open( fnInput );
		input = new ImpInfo( IJ.getImage() );
		input.loadHeader();
		input.showInBdv( bdv );
		input.bdvSource.setColor( new ARGBType( 0x00ff00ff ) );
		input.bdvSource.setCurrent();

		final Actions actions = new Actions( new InputTriggerConfig() );
		actions.install( bdv.getBdvHandle().getKeybindings(), "dzne" );
		actions.runnableAction( this::saveManualRegistration, "save manual registration", "meta S" );
	}
}
