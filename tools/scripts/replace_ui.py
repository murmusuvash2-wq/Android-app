import re

with open('app/src/main/java/com/example/ui/HomeScreen.kt', 'r') as f:
    content = f.read()

target = """        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, WarmGray),
            modifier = Modifier.fillMaxWidth().height(140.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (defaultPhotoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(defaultPhotoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "My Try-On Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(116.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OffWhite)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OffWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "No photo",
                            tint = WarmGray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "My Try-On Photo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This photo is used for your virtual try-ons.",
                        fontSize = 12.sp,
                        color = SoftCharcoal,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = {
                                val uri = TryOnManager.createTempCameraUri(context)
                                if (uri != null) {
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Charcoal,
                            modifier = Modifier.height(34.dp).weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Camera", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                        Surface(
                            onClick = {
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceVariantColor,
                            border = BorderStroke(1.dp, WarmGray),
                            modifier = Modifier.height(34.dp).weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Gallery", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Charcoal)
                            }
                        }
                    }
                }
            }
        }"""

replacement = """        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Take Photo
            Surface(
                onClick = {
                    val uri = TryOnManager.createTempCameraUri(context)
                    if (uri != null) {
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                color = Charcoal,
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take Photo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "Take Photo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Try on any outfit",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Gallery
            Surface(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                color = SurfaceVariantColor,
                border = BorderStroke(1.dp, WarmGray),
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Charcoal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "Choose Photo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Charcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "From your gallery",
                            fontSize = 12.sp,
                            color = SoftCharcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/HomeScreen.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Target not found")
