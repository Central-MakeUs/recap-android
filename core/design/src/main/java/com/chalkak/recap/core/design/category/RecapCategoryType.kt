package com.chalkak.recap.core.design.category

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RecapCategoryBenefitEventBorder
import com.chalkak.recap.core.design.theme.RecapCategoryBenefitEventContent
import com.chalkak.recap.core.design.theme.RecapCategoryBenefitEventTint
import com.chalkak.recap.core.design.theme.RecapCategoryBookContentBorder
import com.chalkak.recap.core.design.theme.RecapCategoryBookContentContent
import com.chalkak.recap.core.design.theme.RecapCategoryBookContentTint
import com.chalkak.recap.core.design.theme.RecapCategoryInfoKnowledgeBorder
import com.chalkak.recap.core.design.theme.RecapCategoryInfoKnowledgeContent
import com.chalkak.recap.core.design.theme.RecapCategoryInfoKnowledgeTint
import com.chalkak.recap.core.design.theme.RecapCategoryJobCareerBorder
import com.chalkak.recap.core.design.theme.RecapCategoryJobCareerContent
import com.chalkak.recap.core.design.theme.RecapCategoryJobCareerTint
import com.chalkak.recap.core.design.theme.RecapCategoryPlaceRestaurantBorder
import com.chalkak.recap.core.design.theme.RecapCategoryPlaceRestaurantContent
import com.chalkak.recap.core.design.theme.RecapCategoryPlaceRestaurantTint
import com.chalkak.recap.core.design.theme.RecapCategoryRecordCaptureBorder
import com.chalkak.recap.core.design.theme.RecapCategoryRecordCaptureContent
import com.chalkak.recap.core.design.theme.RecapCategoryRecordCaptureTint
import com.chalkak.recap.core.design.theme.RecapCategoryScheduleReservationBorder
import com.chalkak.recap.core.design.theme.RecapCategoryScheduleReservationContent
import com.chalkak.recap.core.design.theme.RecapCategoryScheduleReservationTint
import com.chalkak.recap.core.design.theme.RecapCategoryShoppingProductBorder
import com.chalkak.recap.core.design.theme.RecapCategoryShoppingProductContent
import com.chalkak.recap.core.design.theme.RecapCategoryShoppingProductTint

enum class RecapCategoryType(
    @get:DrawableRes val iconResId: Int,
    @get:StringRes val labelResId: Int,
    val borderColor: Color,
    val contentColor: Color,
    val tintColor: Color,
) {
    JobCareer(
        iconResId = R.drawable.ic_edit_16,
        labelResId = R.string.home_category_job_career,
        borderColor = RecapCategoryJobCareerBorder,
        contentColor = RecapCategoryJobCareerContent,
        tintColor = RecapCategoryJobCareerTint,
    ),
    ShoppingProduct(
        iconResId = R.drawable.ic_cart_16,
        labelResId = R.string.home_category_shopping_product,
        borderColor = RecapCategoryShoppingProductBorder,
        contentColor = RecapCategoryShoppingProductContent,
        tintColor = RecapCategoryShoppingProductTint,
    ),
    PlaceRestaurant(
        iconResId = R.drawable.ic_location_16,
        labelResId = R.string.home_category_place_restaurant,
        borderColor = RecapCategoryPlaceRestaurantBorder,
        contentColor = RecapCategoryPlaceRestaurantContent,
        tintColor = RecapCategoryPlaceRestaurantTint,
    ),
    ScheduleReservation(
        iconResId = R.drawable.ic_clock_16,
        labelResId = R.string.home_category_schedule_reservation,
        borderColor = RecapCategoryScheduleReservationBorder,
        contentColor = RecapCategoryScheduleReservationContent,
        tintColor = RecapCategoryScheduleReservationTint,
    ),
    InfoKnowledge(
        iconResId = R.drawable.ic_lightbulb_16,
        labelResId = R.string.home_category_info_knowledge,
        borderColor = RecapCategoryInfoKnowledgeBorder,
        contentColor = RecapCategoryInfoKnowledgeContent,
        tintColor = RecapCategoryInfoKnowledgeTint,
    ),
    BookContent(
        iconResId = R.drawable.ic_document_16,
        labelResId = R.string.home_category_book_content,
        borderColor = RecapCategoryBookContentBorder,
        contentColor = RecapCategoryBookContentContent,
        tintColor = RecapCategoryBookContentTint,
    ),
    BenefitEvent(
        iconResId = R.drawable.ic_star_16,
        labelResId = R.string.home_category_benefit_event,
        borderColor = RecapCategoryBenefitEventBorder,
        contentColor = RecapCategoryBenefitEventContent,
        tintColor = RecapCategoryBenefitEventTint,
    ),
    RecordCapture(
        iconResId = R.drawable.ic_edit_16,
        labelResId = R.string.home_category_record_capture,
        borderColor = RecapCategoryRecordCaptureBorder,
        contentColor = RecapCategoryRecordCaptureContent,
        tintColor = RecapCategoryRecordCaptureTint,
    ),
}
