/**
 * This example shows how to use the Http class in Self to access XML web services.
 */
state GeoNames {
    pattern "Where is *" template lookupGeoNames();
    
    function lookupGeoNames() {
        debug(star);
        return Http.requestXML("http://api.geonames.org/postalCodeSearch?maxRows=1&username=botlibre&postalcode=" + star, "code/name");
    }
}