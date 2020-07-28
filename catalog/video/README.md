<!--
/*
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
-->

# [Codice Alliance Video](http://github.com/codice/alliance/catalog/video)
## Overview
The Alliance Video App provides ingest, metadata extraction, and content handling of MPEG transport stream including STANAG 4609 compliant data.  
The ingestion capability supports both files and streaming data.  Streaming video data is received via UDP endpoint.  

## Sample Data
This project includes sample MPEG TS data for testing purposes.  
- [Closed_Caption_EIA_MPEG2.ts](https://github.com/codice/alliance/blob/master/catalog/video/video-mpegts-stream/src/test/resources/Closed_Caption_EIA_MPEG2.ts)
- [dayflight.mpg](https://github.com/codice/alliance/blob/master/libs/stanag4609/src/test/resources/dayflight.mpg)
- [2019-06-11-14.58.44.129_0_NULL_NULL_000000_4609.ts](https://github.com/codice/alliance/blob/master/distribution/test/itests/test-itests-common/src/main/resources/2019-06-11-14.58.44.129_0_NULL_NULL_000000_4609.ts)

## Stream Configuration and Control
To configure Alliance Video to receive video streams, browse to the Admin UI / Video / FMV Stream Management application.

Admins can configure multiple endpoints to receive data via UDP.  Note that the stream configuration has several properties, but the defaults can be used.
![FMV Stream Configuration] (FMV-stream-config.png)

Once a stream is configured, use the "play" button to have Alliance start listening on the UDP endpoint configured
![FMV Stream Control] (FMV-stream-control.png)

## Streaming data to Alliance
There are multiple tools that can broadcast transport stream data via UDP.  Ffmpeg and TSduck are commonly used.
- Using FFmpeg: `ffmpeg -re -i Night_Flight_IR.mpg -map 0 -c copy -f mpegts udp://127.0.0.1:50000`
- Using TSduck: `tsp -I file Night_Flight_IR.mpg -P regulate -O ip 127.0.0.1:50000`
