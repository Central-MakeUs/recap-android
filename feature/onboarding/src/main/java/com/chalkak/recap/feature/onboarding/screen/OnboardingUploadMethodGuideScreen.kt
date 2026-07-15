package com.chalkak.recap.feature.onboarding.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.RecapLogo
import com.chalkak.recap.core.design.component.RecapLogoAspectRatio
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3
import com.chalkak.recap.feature.onboarding.OnboardingAction
import com.chalkak.recap.feature.onboarding.OnboardingPreviewContainer
import com.chalkak.recap.feature.onboarding.OnboardingScreenPreview
import com.chalkak.recap.feature.onboarding.component.StepHeader

private val UploadMethodGuideCardShape = RoundedCornerShape(10.dp)
private val UploadMethodGuideCardHeight = 129.dp
private val AlbumSelectGuideImageSize = 119.dp

@Composable
fun OnboardingUploadMethodGuideScreen(
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            RecapLogo(
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 24.dp)
                    .width(58.dp)
                    .aspectRatio(RecapLogoAspectRatio),
            )
            StepHeader(
                title = stringResource(R.string.onboarding_upload_method_title),
                description = stringResource(R.string.onboarding_upload_method_description),
                modifier = Modifier.padding(top = 10.dp),
            )
            AlbumSelectGuideCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
            )
            ShareSendGuideCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
        }
        RecapButton(
            text = stringResource(R.string.onboarding_upload_method_confirm_button),
            onClick = { onAction(OnboardingAction.ConfirmUploadMethodGuide) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 11.dp),
            colors = RecapBlue300,
        )
    }
}

@Composable
private fun AlbumSelectGuideCard(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(AlbumSelectGuideImageSize)
            .clip(UploadMethodGuideCardShape)
            .background(RecapGray50)
            .padding(start = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.onboarding_upload_method_album_select),
            contentDescription = stringResource(
                R.string.onboarding_upload_method_album_image_content_description,
            ),
            modifier = Modifier
                .size(AlbumSelectGuideImageSize),
            contentScale = ContentScale.Crop,
        )
        UploadMethodGuideCardText(
            title = stringResource(R.string.onboarding_upload_method_album_title),
            description = stringResource(R.string.onboarding_upload_method_album_description),
            modifier = Modifier
                .weight(1f)
                .padding(end = 20.dp),
        )
    }
}

@Composable
private fun ShareSendGuideCard(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(UploadMethodGuideCardHeight)
            .clip(UploadMethodGuideCardShape)
            .background(RecapGray50),
    ) {
        UploadMethodGuideCardText(
            title = stringResource(R.string.onboarding_upload_method_share_title),
            description = stringResource(R.string.onboarding_upload_method_share_description),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(0.55f)
                .padding(start = 20.dp, top = 17.dp, bottom = 17.dp, end = 8.dp),
        )
        Image(
            painter = painterResource(R.drawable.onboarding_upload_method_share),
            contentDescription = stringResource(
                R.string.onboarding_upload_method_share_image_content_description,
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxHeight()
                .fillMaxWidth(0.45f),
            alignment = Alignment.BottomEnd,
        )
    }
}

@Composable
private fun UploadMethodGuideCardText(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = RecapHeading3,
            color = RecapGray700,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = description,
            style = RecapBody2,
            color = RecapGray500,
        )
    }
}

@OnboardingScreenPreview
@Composable
private fun OnboardingUploadMethodGuideScreenPreview() {
    OnboardingPreviewContainer {
        OnboardingUploadMethodGuideScreen(onAction = {})
    }
}
