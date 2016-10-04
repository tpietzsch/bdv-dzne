package bdv.dzne;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import ij.plugin.PlugIn;
import net.imagej.patcher.LegacyEnvironment;

public class MhdPlayground implements PlugIn
{
	public static void main( final String[] args ) throws ClassNotFoundException
	{
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

	public void doit() throws IOException
	{
		final String fn = "/Users/Pietzsch/Desktop/data/Christopher/Christopher Atlas/canon_T1_r(2).mha";
//		final String fn = "/Users/Pietzsch/Desktop/data/Christopher/C2-Fused-1_turned_mirrored.mhd";

		final BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream( fn ) ) );
		final PartiallyParsedMhd header = new PartiallyParsedMhd( in );
		in.close();

//		IJ.open( fn );
//		final ImagePlus imp = new ImagePlus( fn );
//		imp.show();
//		final FileInfo info = imp.getOriginalFileInfo();
//		final String fn2 = info.directory + "/" + info.fileName;
	}
}
