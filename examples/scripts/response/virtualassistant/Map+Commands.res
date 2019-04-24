//Map commands

Open map
Opening map
command: { type: "map" }
required: map
keywords: map

//Open map to a certain location: "Open map Prairie Centre Mall"

Pattern("Open map *")
Opening map
command: { type: "map", query:""+star }
required: map
keywords: open map

Pattern("Directions to *")
Opening map
command: { type: "map", directions-to: ""+star }
required: directions
keywords: directions

//Pick a mode from Driving, Walking or Biking (Automatically chooses driving if you do not specify)

//Example: "Bike mode directions to Algonquin College", "Walking Mode directions to 14 lane"

Pattern("* mode directions to *")
Opening map
command: { type: "map", mode: star[0], directions-to:""+star[1] }
required: mode directions to
keywords: directions mode

//Directions, choose to AVOID either Tolls, highways or ferries

//Example: "Avoid tolls directions to 123 Street", "Avoid highways directions to Montreal"

Pattern("Avoid * directions to *")
Opening map
command: { type: "map",  avoid: star[0], directions-to: ""+star[1] }
required: avoid directions to
keywords: directions avoid

// "Directions from 122 Millway Ave to Prairie Centre Mall"

Pattern("Directions from * to *")
Opening map
command: { type: "map", directions-to: ""+star[1], directions-from:""+star[0] }
required: directions from to
keywords: directions from to
