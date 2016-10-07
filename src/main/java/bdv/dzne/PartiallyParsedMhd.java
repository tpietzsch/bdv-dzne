package bdv.dzne;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import net.imglib2.realtransform.AffineTransform3D;

public class PartiallyParsedMhd
{
	// all header lines, except for "continuation" lines of parsed vector fields
	final ArrayList< String > lines;

	boolean found_NDims;

	boolean found_TransformMatrix;

	boolean found_Offset;

	boolean found_CenterOfRotation;

	boolean found_ElementSpacing;

	final double[] transformMatrix;

	final double[] offset;

	final double[] centerOfRotation;

	final double[] elementSpacing;

	public PartiallyParsedMhd( final BufferedReader in ) throws IOException, ParseException
	{
		lines = new ArrayList<>();
		found_NDims = false;
		found_TransformMatrix = false;
		transformMatrix = new double[ 9 ];
		found_Offset = false;
		offset = new double[ 3 ];
		found_CenterOfRotation = false;
		centerOfRotation = new double[ 3 ];
		found_ElementSpacing = false;
		elementSpacing = new double[ 3 ];
		while ( true )
		{
			final String line = in.readLine();
			if ( line == null )
				break;

			lines.add( line );

			if ( line.startsWith( "ElementDataFile" ) )
				break;

			if ( line.startsWith( "NDims" ) )
			{
				final String[] parts = line.split( "\\s+" );
				if ( parts.length != 3 || !parts[ 1 ].equals( "=" ) )
					throw new ParseException( "Failed to parse \"NDims\" specification." );
				if ( !parts[ 2 ].equals( "3" ) )
					throw new IllegalArgumentException( "input .mhd file must have NDims = 3" );
				found_NDims = true;
			}
			else if ( line.startsWith( "TransformMatrix" ) )
			{
				parseVector( line, in, transformMatrix );
				found_TransformMatrix = true;
			}
			else if ( line.startsWith( "Offset" ) )
			{
				parseVector( line, in, offset );
				found_Offset = true;
			}
			else if ( line.startsWith( "CenterOfRotation" ) )
			{
				parseVector( line, in, centerOfRotation );
				found_CenterOfRotation = true;
			}
			else if ( line.startsWith( "ElementSpacing" ) )
			{
				parseVector( line, in, elementSpacing );
				found_ElementSpacing = true;
			}
		}
	}

	private void parseVector( final String initialLine, final BufferedReader in, final double[] vector ) throws IOException, ParseException
	{
		final int n = vector.length;

		String line = initialLine;
		String[] parts = line.split( "\\s+" );
		if ( parts.length < 2 || !parts[ 1 ].equals( "=" ) )
			throw new ParseException( "Failed to parse \"TransformMatrix\" specification." );
		try
		{
			int i = 0;
			int j = 2;
			while ( i < n )
			{
				if ( j < parts.length )
					vector[ i++ ] = Double.parseDouble( parts[ j++ ] );
				else
				{
					line = in.readLine();
					if ( line == null )
						throw new ParseException( "Failed to parse vector. Unexpected end of file." );
					parts = line.split( "\\s+" );
					j = 0;
				}
			}
		}
		catch ( final NumberFormatException e )
		{
			throw new ParseException( "Failed to parse vector of numbers.", e );
		}
	}

	/**
	 *
	 * @param sourceTransform
	 *            is set to the source-to-global transform, that transforms
	 *            source coordinates into the global coordinates
	 */
	public void getSourceTransform( final AffineTransform3D sourceTransform )
	{
		final AffineTransform3D calibration = new AffineTransform3D();
		calibration.set(
				elementSpacing[ 0 ], 0, 0, 0,
				0, elementSpacing[ 1 ], 0, 0,
				0, 0, elementSpacing[ 2 ], 0 );

		final AffineTransform3D calibratedToWorld = new AffineTransform3D();
		calibratedToWorld.set(
				transformMatrix[ 0 ], transformMatrix[ 1 ], transformMatrix[ 2 ], offset[ 0 ],
				transformMatrix[ 3 ], transformMatrix[ 4 ], transformMatrix[ 5 ], offset[ 1 ],
				transformMatrix[ 6 ], transformMatrix[ 7 ], transformMatrix[ 8 ], offset[ 2 ] );

		sourceTransform.set( calibration );
		sourceTransform.preConcatenate( calibratedToWorld );
	}

	public void preConcatenateTransform( final AffineTransform3D transform )
	{
		final AffineTransform3D calibratedToWorld = new AffineTransform3D();
		calibratedToWorld.set(
				transformMatrix[ 0 ], transformMatrix[ 1 ], transformMatrix[ 2 ], offset[ 0 ],
				transformMatrix[ 3 ], transformMatrix[ 4 ], transformMatrix[ 5 ], offset[ 1 ],
				transformMatrix[ 6 ], transformMatrix[ 7 ], transformMatrix[ 8 ], offset[ 2 ] );
		calibratedToWorld.preConcatenate( transform );
		for ( int r = 0; r < 3; ++r )
		{
			for ( int c = 0; c < 3; ++c )
				transformMatrix[ 3 * r + c ] = calibratedToWorld.get( r, c );
			offset[ r ] = calibratedToWorld.get( r, 3 );
		}
	}

	public void printModified()
	{
		final PrintStream out = System.out;
		for ( final String line : lines )
		{
			if ( line.startsWith( "TransformMatrix" ) )
			{
				final StringBuffer sb = new StringBuffer( "TransformMatrix = " );
				for ( int i = 0; i < 9; ++i )
				{
					sb.append( transformMatrix[ i ] );
					if ( i < 8 )
						sb.append( " " );
				}
				out.println( sb );
			}
			else if ( line.startsWith( "Offset" ) )
			{
				final StringBuffer sb = new StringBuffer( "Offset = " );
				for ( int i = 0; i < 3; ++i )
				{
					sb.append( offset[ i ] );
					if ( i < 2 )
						sb.append( " " );
				}
				out.println( sb );
			}
			else if ( line.startsWith( "CenterOfRotation" ) )
			{
				final StringBuffer sb = new StringBuffer( "CenterOfRotation = " );
				for ( int i = 0; i < 3; ++i )
				{
					sb.append( centerOfRotation[ i ] );
					if ( i < 2 )
						sb.append( " " );
				}
				out.println( sb );
			}
			else if ( line.startsWith( "ElementSpacing" ) )
			{
				final StringBuffer sb = new StringBuffer( "ElementSpacing = " );
				for ( int i = 0; i < 3; ++i )
				{
					sb.append( elementSpacing[ i ] );
					if ( i < 2 )
						sb.append( " " );
				}
				out.println( sb );
			}
			else
			{
				out.println( line );
			}
		}
	}
}