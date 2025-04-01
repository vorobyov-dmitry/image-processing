package ua.com.ostrog.photo;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.common.IImageMetadata.IImageMetadataItem;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoAscii;
import org.apache.commons.io.FileUtils;

public class MyTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		test08("@D0@90@D1@80@D0@BC@D0@B5@D0@BD@D1@84@D0@B8@D0@BB@D1@8C@D0@BC.@20@D0@9A@D1@82@D0@BE@20@D1@80@D0@B0@D1@81@D1@81@D0@BA@D0@B0@D0@B6@D0@B5@D1@82@20@D0@BD@D0@B5@D0@B1@D1@8B@D0@BB@D0@B8@D1@86@D1@83");
		test04();
		 
	}
	private static void test08(String s) {
				try {
					System.out.println(URLDecoder.decode(s.replace("@", "%"),"UTF8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
	private static void test07() {
		String path = "D:\\UNIX\\usr\\local\\wbin";
		try {
			File file = new File(path);
			Collection< File> files = FileUtils.listFiles(file, new String[]{"avi","mpg"}, true);
			for (Iterator iterator = files.iterator(); iterator.hasNext();) {
				File fl = (File) iterator.next();
				System.out.println(fl.getName());
				try {
					System.out.println(URLDecoder.decode(fl.getName().replace("@", "%"),"UTF8"));
					String newName = "D:\\cartoon\\"+ URLDecoder.decode(fl.getName().replace("@", "%"),"UTF8");
					String newName2 = "D:\\UNIX\\usr\\local\\wbin\\copy\\"+ URLDecoder.decode(fl.getName().replace("@", "%"),"UTF8");
					File destFile = new File(newName);
					if (!destFile.exists()){
						FileUtils.moveFile(fl, destFile );
					}else{
						File destFile2 = new File(newName2);
						FileUtils.moveFile(fl, destFile2 );
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			//System.out.println(URLDecoder.decode("@D0@90@D0@BB@D0@B8@D1@81@D0@B0@20@D0@B2@20@D0@A1@D1@82@D1@80@D0@B0@D0@BD@D0@B5@20@D0@A7@D1@83@D0@B4@D0@B5@D1@81_1.avi".replace("@", "%"), "UTF8"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			File file = new File(path);
			Collection< File> files = FileUtils.listFiles(file, new String[]{"1"}, true);
			for (Iterator iterator = files.iterator(); iterator.hasNext();) {
				File fl = (File) iterator.next();
				System.out.println(fl.getName());
				try {
					System.out.println(URLDecoder.decode(fl.getName().replace("@", "%"),"UTF8"));
					String newName2 = "D:\\UNIX\\usr\\local\\wbin\\copy\\"+ URLDecoder.decode(fl.getName().replace("@", "%"),"UTF8");
					File destFile2 = new File(newName2);
					FileUtils.moveFile(fl, destFile2 );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			//System.out.println(URLDecoder.decode("@D0@90@D0@BB@D0@B8@D1@81@D0@B0@20@D0@B2@20@D0@A1@D1@82@D1@80@D0@B0@D0@BD@D0@B5@20@D0@A7@D1@83@D0@B4@D0@B5@D1@81_1.avi".replace("@", "%"), "UTF8"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void test08() {
		String path = "D:\\UNIX\\usr\\local\\wbin";
		try {
			File file = new File(path);
			Collection< File> files = FileUtils.listFiles(file, new String[]{"avi","mpg"}, true);
			for (Iterator iterator = files.iterator(); iterator.hasNext();) {
				File fl = (File) iterator.next();
				System.out.println(fl.getName());
				try {
					System.out.println(URLDecoder.decode(fl.getName().replace("@", "%"),"UTF8"));
					String newName = "D:\\cartoon\\"+ URLDecoder.decode(fl.getName().replace("@", "%"),"UTF8");
					File destFile = new File(newName);
					FileUtils.moveFile(fl, destFile );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			//System.out.println(URLDecoder.decode("@D0@90@D0@BB@D0@B8@D1@81@D0@B0@20@D0@B2@20@D0@A1@D1@82@D1@80@D0@B0@D0@BD@D0@B5@20@D0@A7@D1@83@D0@B4@D0@B5@D1@81_1.avi".replace("@", "%"), "UTF8"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void test05() {
	 JFrame vFrame = new JFrame("Vertical Split");
	    vFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    JComponent leftButton = new JButton("Top");
	    JComponent rightButton = new JButton("Bottom 1");
	    JComponent rightButton2 = new JButton("Bottom 2");
	    final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	    final JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	    
	    splitPane.setOneTouchExpandable(true);
	    splitPane.setLeftComponent(leftButton);
	    splitPane.setRightComponent(rightButton);
	    ActionListener oneActionListener = new ActionListener() {
	      public void actionPerformed(ActionEvent event) {
	        splitPane.resetToPreferredSizes();
	      }
	    };
	    ((JButton) rightButton).addActionListener(oneActionListener);
	    ActionListener anotherActionListener = new ActionListener() {
	      public void actionPerformed(ActionEvent event) {
	        splitPane.setDividerLocation(10);
	        splitPane.setContinuousLayout(true);
	      }
	    };
	    ((JButton) leftButton).addActionListener(anotherActionListener);
	    splitPane2.setTopComponent(splitPane);
	    splitPane2.setBottomComponent(rightButton2);
	    splitPane2.setOneTouchExpandable(true);
	    vFrame.getContentPane().add(splitPane2, BorderLayout.CENTER);
	    vFrame.setSize(300, 150);
	    vFrame.setVisible(true);
	}
	private static void test04() {
		Integer i1 = 128;
		Integer i2 = 128;
		System.out.println(i1 == i2);
		Integer i3 = 12;
		Integer i4 = 12;
		
		System.out.println(i3 == i4);
		System.out.println(""+i3.hashCode()+" "+ i4.hashCode());
		System.out.println(i3.hashCode() == i4.hashCode());
		
		System.out.println(boolean.class.getName());
		System.out.println(Boolean.class.getName());
		long l1 = System.currentTimeMillis();
		for(int i = 100000; i > 0; i--) {}
		long l2 = System.currentTimeMillis();
		for(int i = 1; i < 100001; i++) {}
		long l3 = System.currentTimeMillis();
		System.out.println((l2-l1));
		System.out.println((l3-l2));


	}
	private static void test03() {
		try {
			String name = "D:\\toProcessPhoto\\изображение 002.jpg";
	File file  = new File(name); 
	final IImageMetadata metadata = Imaging.getMetadata(file);
	final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
	final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
	TiffField field =exifMetadata.findField(new TagInfo("DateTimeOriginal", 36867,FieldType.ASCII)); 
	if (field!=null){
		Object o =  field.getValue();
		System.out.println(o.getClass().getName());
		System.out.println(o);
	}
} catch (ImageReadException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
	}
	private static void test02() {
		try {
			String name = "D:\\02\\img779.jpg";
	File file  = new File(name); 
	final IImageMetadata metadata = Imaging.getMetadata(file);
	final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
	final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
	List<TiffField>  list01= exifMetadata.getAllFields();
	for (TiffField tiffField : list01) {
		try {
			System.out.print(tiffField.getClass().getName());
			System.out.print(" TagInfo="+tiffField.getTagInfo());
			System.out.print(" FieldTypeName="+tiffField.getFieldTypeName());
			System.out.print(" TagName="+tiffField.getTagName());
			System.out.print(" Value="+tiffField.getValue());
			System.out.println();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	System.out.println("*********************************");
	System.out.println( (exifMetadata.findField(new TagInfo("Model", 272, FieldType.ASCII))).getValue());
	System.out.println( (exifMetadata.findField(new TagInfo("DateTimeOriginal", 36867,FieldType.ASCII))).getValue());
	System.out.println();
} catch (ImageReadException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

		
	}
private static void test01() {
	try {
		String name = "D:\\02\\img779.jpg";
File file  = new File(name); 
final IImageMetadata metadata = Imaging.getMetadata(file);

// System.out.println(metadata);

if (metadata instanceof JpegImageMetadata) {
		final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

		// Jpeg EXIF metadata is stored in a TIFF-based directory structure
		// and is identified with TIFF tags.
		// Here we look for the "x resolution" tag, but
		// we could just as easily search for any other tag.
		//
		// see the TiffConstants file for a list of TIFF tags.

		System.out.println("file: " + file.getPath());

		// print out various interesting EXIF tags.
		printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_XRESOLUTION);
		printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME);
		printTagValue(jpegMetadata,
		        ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
		printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
		printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO);
		printTagValue(jpegMetadata,
		        ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
		printTagValue(jpegMetadata,
		        ExifTagConstants.EXIF_TAG_APERTURE_VALUE);
		printTagValue(jpegMetadata,
		        ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE);
		printTagValue(jpegMetadata,
		        GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
		printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE);
		printTagValue(jpegMetadata,
		        GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
		printTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE);

		System.out.println();

		// simple interface to GPS data
		final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
		if (null != exifMetadata) {
		    final TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
		    if (null != gpsInfo) {
		        final String gpsDescription = gpsInfo.toString();
		        final double longitude = gpsInfo.getLongitudeAsDegreesEast();
		        final double latitude = gpsInfo.getLatitudeAsDegreesNorth();

		        System.out.println("    " + "GPS Description: "
		                + gpsDescription);
		        System.out.println("    "
		                + "GPS Longitude (Degrees East): " + longitude);
		        System.out.println("    "
		                + "GPS Latitude (Degrees North): " + latitude);
		    }
		}

		// more specific example of how to manually access GPS values
		final TiffField gpsLatitudeRefField = jpegMetadata
		        .findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
		final TiffField gpsLatitudeField = jpegMetadata
		        .findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE);
		final TiffField gpsLongitudeRefField = jpegMetadata
		        .findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
		final TiffField gpsLongitudeField = jpegMetadata
		        .findEXIFValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE);
		if (gpsLatitudeRefField != null && gpsLatitudeField != null
		        && gpsLongitudeRefField != null
		        && gpsLongitudeField != null) {
		    // all of these values are strings.
		    final String gpsLatitudeRef = (String) gpsLatitudeRefField.getValue();
		    final RationalNumber gpsLatitude[] = (RationalNumber[]) (gpsLatitudeField
		            .getValue());
		    final String gpsLongitudeRef = (String) gpsLongitudeRefField
		            .getValue();
		    final RationalNumber gpsLongitude[] = (RationalNumber[]) gpsLongitudeField
		            .getValue();

		    final RationalNumber gpsLatitudeDegrees = gpsLatitude[0];
		    final RationalNumber gpsLatitudeMinutes = gpsLatitude[1];
		    final RationalNumber gpsLatitudeSeconds = gpsLatitude[2];

		    final RationalNumber gpsLongitudeDegrees = gpsLongitude[0];
		    final RationalNumber gpsLongitudeMinutes = gpsLongitude[1];
		    final RationalNumber gpsLongitudeSeconds = gpsLongitude[2];

		    // This will format the gps info like so:
		    //
		    // gpsLatitude: 8 degrees, 40 minutes, 42.2 seconds S
		    // gpsLongitude: 115 degrees, 26 minutes, 21.8 seconds E

		    System.out.println("    " + "GPS Latitude: "
		            + gpsLatitudeDegrees.toDisplayString() + " degrees, "
		            + gpsLatitudeMinutes.toDisplayString() + " minutes, "
		            + gpsLatitudeSeconds.toDisplayString() + " seconds "
		            + gpsLatitudeRef);
		    System.out.println("    " + "GPS Longitude: "
		            + gpsLongitudeDegrees.toDisplayString() + " degrees, "
		            + gpsLongitudeMinutes.toDisplayString() + " minutes, "
		            + gpsLongitudeSeconds.toDisplayString() + " seconds "
		            + gpsLongitudeRef);

		}

		System.out.println();

		final List<IImageMetadataItem> items = jpegMetadata.getItems();

		System.out.println();
}
	} catch (ImageReadException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
private static void printTagValue(final JpegImageMetadata jpegMetadata,
        final TagInfo tagInfo) {
    final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
    if (field == null) {
        System.out.println(tagInfo.name + ": " + "Not Found.");
    } else {
        System.out.println(tagInfo.name + ": "
                + field.getValueDescription());
    }
}

}
