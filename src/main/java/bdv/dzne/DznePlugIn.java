package bdv.dzne;

import java.io.File;

import bdv.BigDataViewer;
import bdv.dzne.util.FileChooser;
import bdv.dzne.util.FileChooser.DialogType;
import bdv.dzne.util.FileChooser.SelectionMode;
import bdv.dzne.util.XmlFileFilter;
import bdv.export.ProgressWriter;
import bdv.export.ProgressWriterConsole;
import bdv.viewer.ViewerOptions;
import ij.ImageJ;
import ij.plugin.PlugIn;

public class DznePlugIn implements PlugIn
{
	static String lastDatasetPath = "./export.xml";

	@Override
	public void run( final String arg )
	{
		final File file = FileChooser.chooseFile(
				null,
				lastDatasetPath,
				new XmlFileFilter(),
				"Open XML",
				DialogType.LOAD,
				SelectionMode.FILES_ONLY );
		if ( file != null )
		{
			try
			{
				lastDatasetPath = file.getAbsolutePath();
				final ProgressWriter writer = new ProgressWriterConsole(); // TODO ProgressWriterIJ(), but messes with dependencies for project setup...
				BigDataViewer.open( file.getAbsolutePath(), file.getName(), writer, ViewerOptions.options() );
			}
			catch ( final Exception e )
			{
				throw new RuntimeException( e );
			}
		}
	}

	public static void main( final String[] args )
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		new ImageJ();
		new DznePlugIn().run( null );
	}
}
