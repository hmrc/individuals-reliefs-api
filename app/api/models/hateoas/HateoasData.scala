package api.models.hateoas

/** Marker trait that represents data to be used as parameters to the links that are to be returned for a particular endpoint. This data may be
  * identifiers (e.g. nino and/or other resource id) to embed in links, or data from the response that determines whether or not a particular link
  * should be returned in certain scenarios.
  */
trait HateoasData
