package osm;

import gis.arcgis.ShapeFileWriter;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class BaWu_Plz_Creator {
	
	public static void main(String[] args) {
		String filename = "/Users/stefan/Documents/Spielwiese/data/osm/bawu_plz.osm";
		FeatureCollection<SimpleFeatureType,SimpleFeature> featureCollection = FeatureCollections.newCollection(); 
		OsmRelation2RegionShape osm2region = new OsmRelation2RegionShape(featureCollection);
		osm2region.addFeatureColumn("postal_code");
		osm2region.readOsmAndBuildFeatures(filename);
		new ShapeFileWriter(featureCollection).writeFeatures("/Volumes/parkplatz/Stefan/bawu_plz.shp");
	}

}
