package bdv.dzne;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

import bdv.tools.transformation.TransformedSource;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvStackSource;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;
import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>DZNE>Manual Registration")
public class ManualAtlasRegistration implements Command
{
	public static void main( final String[] args ) throws ClassNotFoundException
	{
		final String fnAtlas = "/Users/Pietzsch/Desktop/data/Christopher/Christopher Atlas/canon_T1_r(2).mha";
		final String fnInput = "/Users/Pietzsch/Desktop/data/Christopher/C2-Fused-1_turned_mirrored.mhd";
//		final String fnAtlas = "/Users/Pietzsch/Desktop/data/Christopher/Results_prototype/atlas.mhd";
//		final String fnInput = "/Users/Pietzsch/Desktop/data/Christopher/Results_prototype/C2-95A.mhd";

		final ImageJ ij = net.imagej.Main.launch( args );
		IJ.open( fnAtlas );
		IJ.open( fnInput );
	}


	@Parameter( label = "atlas" )
	ImagePlus impAtlas = null;

	@Parameter( label = "input" )
	ImagePlus impInput = null;

	@Override
	public void run()
	{
		try
		{
			atlas = new ImpInfo( impAtlas );
			atlas.loadHeader();
			atlas.showInBdv( null );
			atlas.bdvSource.setColor( new ARGBType( 0x0000ff00 ) );
			bdv = atlas.bdvSource;

			input = new ImpInfo( impInput );
			input.loadHeader();
			input.showInBdv( bdv );
			input.bdvSource.setColor( new ARGBType( 0x00ff00ff ) );
			input.bdvSource.setCurrent();

			final Actions actions = new Actions( new InputTriggerConfig() );
			actions.install( bdv.getBdvHandle().getKeybindings(), "dzne" );
			actions.runnableAction( this::saveManualRegistration, "save manual registration", "meta S", "ctrl S" );
		}
		catch ( final IOException e )
		{
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
		System.out.println( "========================" );

		final AffineTransform3D transform = getManualTransform( input.bdvSource ).preConcatenate( getManualTransform( atlas.bdvSource ).inverse() );
		input.header.setPreConcatenatedTransform( transform );
		input.header.printModified();

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
}
