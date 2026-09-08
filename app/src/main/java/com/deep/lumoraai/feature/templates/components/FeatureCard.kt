package com.deep.lumoraai.feature.templates.components

import android.net.Uri

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.deep.lumoraai.feature.templates.model.TemplateListItem

private val CardColor = Color(0xFF0E172A)
private val CardStroke = Color(0xFF1B2A44)
private val Muted = Color(0xFF9BA6BA)
private val Lime = Color(0xFFD6FF2F)

@Composable
fun FeatureCard(
    item: TemplateListItem,
    onClick: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        shape = RoundedCornerShape(12.dp),
        color = CardColor,
        border = BorderStroke(
            1.dp,
            CardStroke
        )
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            /*
             * Large preview image
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
            ) {

                AsyncImage(
                    model =
                        "file:///android_asset/templates/" +
                            Uri.encode(item.assetFileName),

                    contentDescription =
                        item.title,

                    contentScale =
                        ContentScale.Crop,

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp
                                )
                            )
                )
            }

            /*
             * Information section
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f),

                    verticalArrangement =
                        Arrangement.Center
                ) {

                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier =
                            Modifier.height(3.dp)
                    )

                    Text(
                        text = item.subtitle,
                        color = Muted,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        maxLines = 2,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(10.dp)
                )

                /*
                 * Copy prompt button
                 */
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Color.White.copy(
                                alpha = 0.055f
                            )
                        )
                        .clickable(
                            onClick = onCopy
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.ContentCopy,

                        contentDescription =
                            "Copy prompt",

                        tint =
                            Lime.copy(
                                alpha = 0.95f
                            ),

                        modifier =
                            Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}