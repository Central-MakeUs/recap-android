package com.chalkak.recap.core.design.category

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RecapCategoryBenefitEvent300
import com.chalkak.recap.core.design.theme.RecapCategoryBenefitEvent500
import com.chalkak.recap.core.design.theme.RecapCategoryBenefitEvent700
import com.chalkak.recap.core.design.theme.RecapCategoryBookContent300
import com.chalkak.recap.core.design.theme.RecapCategoryBookContent500
import com.chalkak.recap.core.design.theme.RecapCategoryBookContent700
import com.chalkak.recap.core.design.theme.RecapCategoryInfoKnowledge300
import com.chalkak.recap.core.design.theme.RecapCategoryInfoKnowledge500
import com.chalkak.recap.core.design.theme.RecapCategoryInfoKnowledge700
import com.chalkak.recap.core.design.theme.RecapCategoryJobCareer300
import com.chalkak.recap.core.design.theme.RecapCategoryJobCareer500
import com.chalkak.recap.core.design.theme.RecapCategoryJobCareer700
import com.chalkak.recap.core.design.theme.RecapCategoryPlaceRestaurant300
import com.chalkak.recap.core.design.theme.RecapCategoryPlaceRestaurant500
import com.chalkak.recap.core.design.theme.RecapCategoryPlaceRestaurant700
import com.chalkak.recap.core.design.theme.RecapCategoryRecordCapture300
import com.chalkak.recap.core.design.theme.RecapCategoryRecordCapture500
import com.chalkak.recap.core.design.theme.RecapCategoryRecordCapture700
import com.chalkak.recap.core.design.theme.RecapCategoryScheduleReservation300
import com.chalkak.recap.core.design.theme.RecapCategoryScheduleReservation500
import com.chalkak.recap.core.design.theme.RecapCategoryScheduleReservation700
import com.chalkak.recap.core.design.theme.RecapCategoryShoppingProduct300
import com.chalkak.recap.core.design.theme.RecapCategoryShoppingProduct500
import com.chalkak.recap.core.design.theme.RecapCategoryShoppingProduct700

enum class RecapCategoryType(
    @get:DrawableRes val iconResId: Int,
    @get:StringRes val labelResId: Int,
    val borderColor: Color,
    val contentColor: Color,
    val tintColor: Color,
) {
    ShoppingProduct(
        iconResId = R.drawable.ic_cart_16,
        labelResId = R.string.home_category_shopping_product,
        borderColor = RecapCategoryShoppingProduct500,
        contentColor = RecapCategoryShoppingProduct700,
        tintColor = RecapCategoryShoppingProduct300,
    ),
    PlaceRestaurant(
        iconResId = R.drawable.ic_location_16,
        labelResId = R.string.home_category_place_restaurant,
        borderColor = RecapCategoryPlaceRestaurant500,
        contentColor = RecapCategoryPlaceRestaurant700,
        tintColor = RecapCategoryPlaceRestaurant300,
    ),
    ScheduleReservation(
        iconResId = R.drawable.ic_clock_16,
        labelResId = R.string.home_category_schedule_reservation,
        borderColor = RecapCategoryScheduleReservation500,
        contentColor = RecapCategoryScheduleReservation700,
        tintColor = RecapCategoryScheduleReservation300,
    ),
    InfoKnowledge(
        iconResId = R.drawable.ic_lightbulb_16,
        labelResId = R.string.home_category_info_knowledge,
        borderColor = RecapCategoryInfoKnowledge500,
        contentColor = RecapCategoryInfoKnowledge700,
        tintColor = RecapCategoryInfoKnowledge300,
    ),
    BookContent(
        iconResId = R.drawable.ic_document_16,
        labelResId = R.string.home_category_book_content,
        borderColor = RecapCategoryBookContent500,
        contentColor = RecapCategoryBookContent700,
        tintColor = RecapCategoryBookContent300,
    ),
    BenefitEvent(
        iconResId = R.drawable.ic_star_16,
        labelResId = R.string.home_category_benefit_event,
        borderColor = RecapCategoryBenefitEvent500,
        contentColor = RecapCategoryBenefitEvent700,
        tintColor = RecapCategoryBenefitEvent300,
    ),
    RecordCapture(
        iconResId = R.drawable.ic_edit_16,
        labelResId = R.string.home_category_record_capture,
        borderColor = RecapCategoryRecordCapture500,
        contentColor = RecapCategoryRecordCapture700,
        tintColor = RecapCategoryRecordCapture300,
    ),
    JobCareer(
        iconResId = R.drawable.ic_person_16,
        labelResId = R.string.home_category_job_career,
        borderColor = RecapCategoryJobCareer500,
        contentColor = RecapCategoryJobCareer700,
        tintColor = RecapCategoryJobCareer300,
    ),
}
