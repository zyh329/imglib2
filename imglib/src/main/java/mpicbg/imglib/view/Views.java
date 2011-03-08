package mpicbg.imglib.view;

import mpicbg.imglib.img.Img;
import mpicbg.imglib.outofbounds.OutOfBoundsFactory;
import mpicbg.imglib.outofbounds.OutOfBoundsMirrorFactory;
import mpicbg.imglib.util.Util;

public class Views
{
	public static < T > View< T > view( final Img< T > img, final OutOfBoundsFactory< T, Img< T > > factory )
	{
		return new ExtendableRandomAccessibleIntervalImp< T, Img< T > >( img, factory );
	}

	public static < T > View< T > view( final Img< T > img )
	{
		return view( img, new OutOfBoundsMirrorFactory< T, Img< T > >(OutOfBoundsMirrorFactory.Boundary.SINGLE) );
	}

	public static < T > View< T > view( final View< T > view, final OutOfBoundsFactory< T, View< T > > factory )
	{
		return new ExtendableRandomAccessibleIntervalImp< T, View< T > >( view, factory );
	}

	public static < T > View< T > view( final View< T > view )
	{
		return view( view, new OutOfBoundsMirrorFactory< T, View< T > >(OutOfBoundsMirrorFactory.Boundary.SINGLE) );
	}

	public static < T > View< T > superIntervalView( final View< T > view, long[] offset, long[] dimension )
	{
		final int n = view.numDimensions();
		ViewTransform t = new ViewTransform( n, n );
		t.setTranslation( offset );
		return new ViewTransformView< T >( view, t, dimension );
	}

	public static < T > View< T > flippedView( final View< T > view, final int d )
	{
		final int n = view.numDimensions();
		long[] tmp = new long[ n ];
		tmp[ d ] = view.max( d );
		int[] component = new int[ n ];
		boolean[] inv = new boolean[ n ];
		for ( int e = 0; e < n; ++e )
		{
			component[ e ] = e;
			inv[ e ] = (e == d);
		}
		ViewTransform t = new ViewTransform( n, n );
		t.setTranslation( tmp );
		t.setPermutation( component, inv );
		view.dimensions( tmp );
		System.out.println( Util.printCoordinates( tmp ) );
		return new ViewTransformView< T >( view, t, tmp );
	}

	/**
	 * take a (n-1)-dimensional slice of a n-dimensional view,
	 * fixing d-component of coordinates to pos. 
	 */
	public static < T > View< T > hyperSlice( final View< T > view, final int d, long pos )
	{
		final int m = view.numDimensions();
		final int n = m - 1;
		ViewTransform t = new ViewTransform( n, m );
		long[] translation = new long[ m ];
		translation[ d ] = pos;
		boolean[] zero = new boolean[ m ];
		int[] component = new int[ m ];
		long[] dimension = new long[ n ];
		for ( int e = 0; e < m; ++e )
		{
			if ( e < d )
			{
				zero [ e ] = false;
				component[ e ] = e;
				dimension[ e ] = view.dimension( e );
			}
			else if ( e > d )
			{
				zero [ e ] = false;
				component[ e ] = e - 1;
				dimension[ e - 1 ] = view.dimension( e );
			}
			else
			{
				zero [ e ] = true;
				component[ e ] = 0;
			}
		}
		t.setTranslation( translation );
		t.setPermutation( zero, component );
		return new ViewTransformView< T >( view, t, dimension );
	}

	/**
	 * Create view that is rotated by 90 degrees.
	 * The rotation is specified by the fromAxis and toAxis arguments.
	 * 
	 * If fromAxis=0 and toAxis=1, this means that the X-axis of the source view
	 * is mapped to the Y-Axis of the rotated view. That is, it corresponds to a
	 * 90 degree clock-wise rotation of the source view in the XY plane.
	 * 
	 * fromAxis=1 and toAxis=0 corresponds to a counter-clock-wise rotation in the
	 * XY plane.
	 */
	public static < T > View< T > rotatedView( final View< T > view, final int fromAxis, final int toAxis )
	{
		final int n = view.numDimensions();
		long[] tmp = new long[ n ];
		tmp[ toAxis ] = view.max( toAxis );
		int[] component = new int[ n ];
		boolean[] inv = new boolean[ n ];
		for ( int e = 0; e < n; ++e )
		{
			if ( e == toAxis )
			{
				component[ e ] = fromAxis;
				inv[ e ] = true;
			}
			else if ( e == fromAxis )
			{
				component[ e ] = toAxis;
			}
			else
			{
				component[ e ] = e;
			}
		}
		ViewTransform t = new ViewTransform( n, n );
		t.setTranslation( tmp );
		t.setPermutation( component, inv );
		view.dimensions( tmp );
		final long fromDim = tmp[ toAxis ];
		tmp[ toAxis ] = tmp[ fromAxis ];
		tmp[ fromAxis ] = fromDim;
		return new ViewTransformView< T >( view, t, tmp );
	}
}
