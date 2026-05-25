package com.example.autoai.localization

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.example.autoai.R

object AppStrings {

    object Auth {
        @StringRes
        val passwordsDoNotMatchRes = R.string.auth_register_passwords_do_not_match

        @StringRes
        val registerSuccessRes = R.string.auth_register_success

        @StringRes
        val loginSuccessRes = R.string.auth_login_success

        @StringRes
        val garageTitleRes = R.string.auth_garage_title

        @StringRes
        val nameLabelRes = R.string.auth_name_label

        @StringRes
        val namePlaceholderRes = R.string.auth_name_placeholder

        @StringRes
        val emailLabelRes = R.string.auth_email_label

        @StringRes
        val emailPlaceholderRes = R.string.auth_email_placeholder

        @StringRes
        val passwordLabelRes = R.string.auth_password_label

        @StringRes
        val passwordPlaceholderRes = R.string.auth_password_placeholder

        @StringRes
        val confirmPasswordLabelRes = R.string.auth_confirm_password_label

        @StringRes
        val createAccountButtonRes = R.string.auth_create_account_button

        @StringRes
        val loginButtonRes = R.string.auth_login_button

        @StringRes
        val forgotPasswordRes = R.string.auth_forgot_password

        @StringRes
        val loginTabRes = R.string.auth_login_tab

        @StringRes
        val registerTabRes = R.string.auth_register_tab

        @StringRes
        val registerInvalidEmailRes = R.string.auth_register_invalid_email

        val passwordsDoNotMatch: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(passwordsDoNotMatchRes)

        val registerSuccess: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(registerSuccessRes)

        val loginSuccess: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(loginSuccessRes)

        val garageTitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(garageTitleRes)

        val nameLabel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(nameLabelRes)

        val namePlaceholder: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(namePlaceholderRes)

        val emailLabel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(emailLabelRes)

        val emailPlaceholder: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(emailPlaceholderRes)

        val passwordLabel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(passwordLabelRes)

        val passwordPlaceholder: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(passwordPlaceholderRes)

        val confirmPasswordLabel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(confirmPasswordLabelRes)

        val createAccountButton: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(createAccountButtonRes)

        val loginButton: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(loginButtonRes)

        val forgotPassword: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(forgotPasswordRes)

        val loginTab: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(loginTabRes)

        val registerTab: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(registerTabRes)

        val registerInvalidEmail: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(registerInvalidEmailRes)
    }

    object Common {
        @StringRes
        val backRes = R.string.common_back

        @StringRes
        val showPasswordRes = R.string.common_show_password

        @StringRes
        val hidePasswordRes = R.string.common_hide_password

        val back: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(backRes)

        val showPassword: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(showPasswordRes)

        val hidePassword: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(hidePasswordRes)
    }

    object Home {
        // greetingRes is parameterised (%1$s) — call stringResource(greetingRes, userName) directly
        @StringRes
        val greetingRes = R.string.home_greeting

        @StringRes
        val monthlyCostLabelRes = R.string.home_monthly_cost_label

        @StringRes
        val actionFuelRes = R.string.home_action_fuel

        @StringRes
        val actionServiceRes = R.string.home_action_service

        @StringRes
        val actionAiRes = R.string.home_action_ai

        @StringRes
        val navHomeRes = R.string.home_nav_home

        @StringRes
        val navGarageRes = R.string.home_nav_garage

        @StringRes
        val navCostsRes = R.string.home_nav_costs

        @StringRes
        val navRemindersRes = R.string.home_nav_reminders

        @StringRes
        val navAiChatRes = R.string.home_nav_ai_chat

        @StringRes
        val noActiveVehicleTitleRes = R.string.home_no_active_vehicle_title

        @StringRes
        val noActiveVehicleSubtitleRes = R.string.home_no_active_vehicle_subtitle

        @StringRes
        val profileButtonDescriptionRes = R.string.home_profile_button_description

        val profileButtonDescription: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(profileButtonDescriptionRes)

        val monthlyCostLabel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(monthlyCostLabelRes)

        val actionFuel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(actionFuelRes)

        val actionService: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(actionServiceRes)

        val actionAi: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(actionAiRes)

        val navHome: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(navHomeRes)

        val navGarage: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(navGarageRes)

        val navCosts: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(navCostsRes)

        val navReminders: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(navRemindersRes)

        val navAiChat: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(navAiChatRes)

        val noActiveVehicleTitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(noActiveVehicleTitleRes)

        val noActiveVehicleSubtitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(noActiveVehicleSubtitleRes)
    }

    object Garage {
        @StringRes
        val titleRes = R.string.garage_title

        @StringRes
        val subtitleRes = R.string.garage_subtitle

        @StringRes
        val emptyTitleRes = R.string.garage_empty_title

        @StringRes
        val emptyDescriptionRes = R.string.garage_empty_description

        @StringRes
        val addVehicleRes = R.string.garage_add_vehicle

        @StringRes
        val noLicensePlateRes = R.string.garage_no_license_plate

        @StringRes
        val activeVehicleContentDescriptionRes = R.string.garage_active_vehicle_content_description

        val title: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(titleRes)

        val subtitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(subtitleRes)

        val emptyTitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(emptyTitleRes)

        val emptyDescription: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(emptyDescriptionRes)

        val addVehicle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(addVehicleRes)

        val noLicensePlate: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(noLicensePlateRes)

        val activeVehicleContentDescription: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(activeVehicleContentDescriptionRes)

        @StringRes
        val vehicleMenuEditRes = R.string.garage_vehicle_menu_edit

        @StringRes
        val vehicleMenuDeleteRes = R.string.garage_vehicle_menu_delete

        @StringRes
        val vehicleDeleteDialogTitleRes = R.string.garage_vehicle_delete_dialog_title

        @StringRes
        val vehicleDeleteDialogMessageRes = R.string.garage_vehicle_delete_dialog_message

        @StringRes
        val vehicleDeleteConfirmRes = R.string.garage_vehicle_delete_confirm

        @StringRes
        val vehicleDeleteCancelRes = R.string.garage_vehicle_delete_cancel

        @StringRes
        val vehicleDeletedSnackbarRes = R.string.garage_vehicle_deleted_snackbar

        @StringRes
        val undoRes = R.string.garage_undo

        val vehicleMenuEdit: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(vehicleMenuEditRes)

        val vehicleMenuDelete: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(vehicleMenuDeleteRes)

        val vehicleDeleteDialogTitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(vehicleDeleteDialogTitleRes)

        val vehicleDeleteDialogMessage: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(vehicleDeleteDialogMessageRes)

        val vehicleDeleteConfirm: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(vehicleDeleteConfirmRes)

        val vehicleDeleteCancel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(vehicleDeleteCancelRes)

        val undo: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(undoRes)
    }

    object AddVehicle {
        @StringRes
        val titleRes = R.string.add_vehicle_title

        @StringRes
        val subtitleRes = R.string.add_vehicle_subtitle

        @StringRes
        val makeLabelRes = R.string.add_vehicle_make_label

        @StringRes
        val makePlaceholderRes = R.string.add_vehicle_make_placeholder

        @StringRes
        val modelLabelRes = R.string.add_vehicle_model_label

        @StringRes
        val modelPlaceholderRes = R.string.add_vehicle_model_placeholder

        @StringRes
        val yearLabelRes = R.string.add_vehicle_year_label

        @StringRes
        val yearPlaceholderRes = R.string.add_vehicle_year_placeholder

        @StringRes
        val fuelTypeLabelRes = R.string.add_vehicle_fuel_type_label

        @StringRes
        val mileageLabelRes = R.string.add_vehicle_mileage_label

        @StringRes
        val mileagePlaceholderRes = R.string.add_vehicle_mileage_placeholder

        @StringRes
        val licensePlateLabelRes = R.string.add_vehicle_license_plate_label

        @StringRes
        val licensePlatePlaceholderRes = R.string.add_vehicle_license_plate_placeholder

        @StringRes
        val saveButtonRes = R.string.add_vehicle_save_button

        @StringRes
        val discardDialogTitleRes = R.string.add_vehicle_discard_dialog_title

        @StringRes
        val discardDialogMessageRes = R.string.add_vehicle_discard_dialog_message

        @StringRes
        val discardDialogConfirmRes = R.string.add_vehicle_discard_dialog_confirm

        @StringRes
        val discardDialogDismissRes = R.string.add_vehicle_discard_dialog_dismiss

        @StringRes
        val yearPickerTitleRes = R.string.add_vehicle_year_picker_title

        @StringRes
        val yearPickerCancelRes = R.string.add_vehicle_year_picker_cancel

        @StringRes
        val yearPickerConfirmRes = R.string.add_vehicle_year_picker_confirm

        val title: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(titleRes)

        val subtitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(subtitleRes)

        val makeLabel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(makeLabelRes)

        val makePlaceholder: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(makePlaceholderRes)

        val modelLabel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(modelLabelRes)

        val modelPlaceholder: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(modelPlaceholderRes)

        val yearLabel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(yearLabelRes)

        val yearPlaceholder: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(yearPlaceholderRes)

        val fuelTypeLabel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(fuelTypeLabelRes)

        val mileageLabel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(mileageLabelRes)

        val mileagePlaceholder: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(mileagePlaceholderRes)

        val licensePlateLabel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(licensePlateLabelRes)

        val licensePlatePlaceholder: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(licensePlatePlaceholderRes)

        val saveButton: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(saveButtonRes)

        val discardDialogTitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(discardDialogTitleRes)

        val discardDialogMessage: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(discardDialogMessageRes)

        val discardDialogConfirm: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(discardDialogConfirmRes)

        val discardDialogDismiss: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(discardDialogDismissRes)

        val yearPickerTitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(yearPickerTitleRes)

        val yearPickerCancel: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(yearPickerCancelRes)

        val yearPickerConfirm: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(yearPickerConfirmRes)

        @StringRes
        val editTitleRes = R.string.edit_vehicle_title

        @StringRes
        val editSubtitleRes = R.string.edit_vehicle_subtitle

        @StringRes
        val editSaveButtonRes = R.string.edit_vehicle_save_button

        val editTitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(editTitleRes)

        val editSubtitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(editSubtitleRes)

        val editSaveButton: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(editSaveButtonRes)
    }

    object Reminders {
        @StringRes val titleRes = R.string.reminders_title
        @StringRes val emptyTitleRes = R.string.reminders_empty_title
        @StringRes val addFabDescriptionRes = R.string.reminders_add_fab_description
        @StringRes val addSheetTitleRes = R.string.reminders_add_sheet_title
        @StringRes val titleLabelRes = R.string.reminders_title_label
        @StringRes val titlePlaceholderRes = R.string.reminders_title_placeholder
        @StringRes val dateLabelRes = R.string.reminders_date_label
        @StringRes val datePlaceholderRes = R.string.reminders_date_placeholder
        @StringRes val noteLabelRes = R.string.reminders_note_label
        @StringRes val notePlaceholderRes = R.string.reminders_note_placeholder
        @StringRes val saveButtonRes = R.string.reminders_save_button
        @StringRes val okRes = R.string.reminders_ok
        @StringRes val cancelRes = R.string.reminders_cancel
        @StringRes val deleteDialogTitleRes = R.string.reminders_delete_dialog_title
        @StringRes val deleteDialogMessageRes = R.string.reminders_delete_dialog_message
        @StringRes val deleteConfirmRes = R.string.reminders_delete_confirm
        @StringRes val deleteCancelRes = R.string.reminders_delete_cancel
        @StringRes val editDescriptionRes = R.string.reminders_edit_description
        @StringRes val deleteDescriptionRes = R.string.reminders_delete_description

        val title: String @Composable @ReadOnlyComposable get() = stringResource(titleRes)
        val emptyTitle: String @Composable @ReadOnlyComposable get() = stringResource(emptyTitleRes)
        val addFabDescription: String @Composable @ReadOnlyComposable get() = stringResource(addFabDescriptionRes)
        val addSheetTitle: String @Composable @ReadOnlyComposable get() = stringResource(addSheetTitleRes)
        val titleLabel: String @Composable @ReadOnlyComposable get() = stringResource(titleLabelRes)
        val titlePlaceholder: String @Composable @ReadOnlyComposable get() = stringResource(titlePlaceholderRes)
        val dateLabel: String @Composable @ReadOnlyComposable get() = stringResource(dateLabelRes)
        val datePlaceholder: String @Composable @ReadOnlyComposable get() = stringResource(datePlaceholderRes)
        val noteLabel: String @Composable @ReadOnlyComposable get() = stringResource(noteLabelRes)
        val notePlaceholder: String @Composable @ReadOnlyComposable get() = stringResource(notePlaceholderRes)
        val saveButton: String @Composable @ReadOnlyComposable get() = stringResource(saveButtonRes)
        val ok: String @Composable @ReadOnlyComposable get() = stringResource(okRes)
        val cancel: String @Composable @ReadOnlyComposable get() = stringResource(cancelRes)
        val deleteDialogTitle: String @Composable @ReadOnlyComposable get() = stringResource(deleteDialogTitleRes)
        val deleteDialogMessage: String @Composable @ReadOnlyComposable get() = stringResource(deleteDialogMessageRes)
        val deleteConfirm: String @Composable @ReadOnlyComposable get() = stringResource(deleteConfirmRes)
        val deleteCancel: String @Composable @ReadOnlyComposable get() = stringResource(deleteCancelRes)
        val editDescription: String @Composable @ReadOnlyComposable get() = stringResource(editDescriptionRes)
        val deleteDescription: String @Composable @ReadOnlyComposable get() = stringResource(deleteDescriptionRes)
    }

    object Settings {
        @StringRes val title = R.string.settings_title
        @StringRes val notifications = R.string.settings_notifications
        @StringRes val darkMode = R.string.settings_dark_mode
        @StringRes val language = R.string.settings_language
        @StringRes val privacyPolicy = R.string.settings_privacy_policy
        @StringRes val logout = R.string.settings_logout
        @StringRes val comingSoon = R.string.settings_coming_soon
        @StringRes val aiAutoReminders = R.string.settings_ai_auto_reminders
        @StringRes val currency = R.string.settings_currency
        @StringRes val currencyDialogTitle = R.string.settings_currency_dialog_title
        @StringRes val currencyDialogCancel = R.string.settings_currency_dialog_cancel
        @StringRes val currencyUpdatedRes = R.string.settings_currency_updated

        val titleStr: String @Composable @ReadOnlyComposable get() = stringResource(title)
        val notificationsStr: String @Composable @ReadOnlyComposable get() = stringResource(notifications)
        val darkModeStr: String @Composable @ReadOnlyComposable get() = stringResource(darkMode)
        val languageStr: String @Composable @ReadOnlyComposable get() = stringResource(language)
        val privacyPolicyStr: String @Composable @ReadOnlyComposable get() = stringResource(privacyPolicy)
        val logoutStr: String @Composable @ReadOnlyComposable get() = stringResource(logout)
        val comingSoonStr: String @Composable @ReadOnlyComposable get() = stringResource(comingSoon)
        val aiAutoRemindersStr: String @Composable @ReadOnlyComposable get() = stringResource(aiAutoReminders)
        val currencyStr: String @Composable @ReadOnlyComposable get() = stringResource(currency)
        val currencyDialogTitleStr: String @Composable @ReadOnlyComposable get() = stringResource(currencyDialogTitle)
        val currencyDialogCancelStr: String @Composable @ReadOnlyComposable get() = stringResource(currencyDialogCancel)
    }

    object Chat {
        @StringRes val titleRes = R.string.chat_title
        @StringRes val subtitleRes = R.string.chat_subtitle
        @StringRes val placeholderRes = R.string.chat_placeholder
        @StringRes val sendDescriptionRes = R.string.chat_send_description
        @StringRes val emptyStateTitleRes = R.string.chat_empty_state_title
        @StringRes val removeImageDescriptionRes = R.string.chat_remove_image_description
        @StringRes val pickImageDescriptionRes = R.string.chat_pick_image_description
        @StringRes val takePhotoDescriptionRes = R.string.chat_take_photo_description

        val title: String @Composable @ReadOnlyComposable get() = stringResource(titleRes)
        val subtitle: String @Composable @ReadOnlyComposable get() = stringResource(subtitleRes)
        val placeholder: String @Composable @ReadOnlyComposable get() = stringResource(placeholderRes)
        val sendDescription: String @Composable @ReadOnlyComposable get() = stringResource(sendDescriptionRes)
        val emptyStateTitle: String @Composable @ReadOnlyComposable get() = stringResource(emptyStateTitleRes)
        val removeImageDescription: String @Composable @ReadOnlyComposable get() = stringResource(removeImageDescriptionRes)
        val pickImageDescription: String @Composable @ReadOnlyComposable get() = stringResource(pickImageDescriptionRes)
        val takePhotoDescription: String @Composable @ReadOnlyComposable get() = stringResource(takePhotoDescriptionRes)
    }

    object Costs {
        @StringRes val titleRes = R.string.costs_title
        @StringRes val tabHistoryRes = R.string.costs_tab_history
        @StringRes val tabStatisticsRes = R.string.costs_tab_statistics
        @StringRes val emptyTitleRes = R.string.costs_empty_title
        @StringRes val emptyDescriptionRes = R.string.costs_empty_description
        @StringRes val addSheetTitleRes = R.string.costs_add_sheet_title
        @StringRes val categoryLabelRes = R.string.costs_category_label
        @StringRes val categoryFuelRes = R.string.costs_category_fuel
        @StringRes val categoryServiceRes = R.string.costs_category_service
        @StringRes val categoryTiresRes = R.string.costs_category_tires
        @StringRes val categoryEquipmentRes = R.string.costs_category_equipment
        @StringRes val categoryOtherRes = R.string.costs_category_other
        @StringRes val amountLabelRes = R.string.costs_amount_label
        @StringRes val amountPlaceholderRes = R.string.costs_amount_placeholder
        @StringRes val locationLabelRes = R.string.costs_location_label
        @StringRes val locationPlaceholderRes = R.string.costs_location_placeholder
        @StringRes val descriptionLabelRes = R.string.costs_description_label
        @StringRes val descriptionPlaceholderRes = R.string.costs_description_placeholder
        @StringRes val saveButtonRes = R.string.costs_save_button
        @StringRes val addFabDescriptionRes = R.string.costs_add_fab_description
        @StringRes val statsTotalLabelRes = R.string.costs_stats_total_label
        @StringRes val statsByCategoryLabelRes = R.string.costs_stats_by_category_label
        @StringRes val successRes = R.string.costs_success
        @StringRes val errorInvalidAmountRes = R.string.costs_error_invalid_amount
        @StringRes val noActiveVehicleTitleRes = R.string.costs_no_active_vehicle_title
        @StringRes val noActiveVehicleSubtitleRes = R.string.costs_no_active_vehicle_subtitle

        val title: String @Composable @ReadOnlyComposable get() = stringResource(titleRes)
        val tabHistory: String @Composable @ReadOnlyComposable get() = stringResource(tabHistoryRes)
        val tabStatistics: String @Composable @ReadOnlyComposable get() = stringResource(tabStatisticsRes)
        val emptyTitle: String @Composable @ReadOnlyComposable get() = stringResource(emptyTitleRes)
        val emptyDescription: String @Composable @ReadOnlyComposable get() = stringResource(emptyDescriptionRes)
        val addSheetTitle: String @Composable @ReadOnlyComposable get() = stringResource(addSheetTitleRes)
        val categoryLabel: String @Composable @ReadOnlyComposable get() = stringResource(categoryLabelRes)
        val categoryFuel: String @Composable @ReadOnlyComposable get() = stringResource(categoryFuelRes)
        val categoryService: String @Composable @ReadOnlyComposable get() = stringResource(categoryServiceRes)
        val categoryTires: String @Composable @ReadOnlyComposable get() = stringResource(categoryTiresRes)
        val categoryEquipment: String @Composable @ReadOnlyComposable get() = stringResource(categoryEquipmentRes)
        val categoryOther: String @Composable @ReadOnlyComposable get() = stringResource(categoryOtherRes)
        val amountLabel: String @Composable @ReadOnlyComposable get() = stringResource(amountLabelRes)
        val amountPlaceholder: String @Composable @ReadOnlyComposable get() = stringResource(amountPlaceholderRes)
        val locationLabel: String @Composable @ReadOnlyComposable get() = stringResource(locationLabelRes)
        val locationPlaceholder: String @Composable @ReadOnlyComposable get() = stringResource(locationPlaceholderRes)
        val descriptionLabel: String @Composable @ReadOnlyComposable get() = stringResource(descriptionLabelRes)
        val descriptionPlaceholder: String @Composable @ReadOnlyComposable get() = stringResource(descriptionPlaceholderRes)
        val saveButton: String @Composable @ReadOnlyComposable get() = stringResource(saveButtonRes)
        val addFabDescription: String @Composable @ReadOnlyComposable get() = stringResource(addFabDescriptionRes)
        val statsTotalLabel: String @Composable @ReadOnlyComposable get() = stringResource(statsTotalLabelRes)
        val statsByCategoryLabel: String @Composable @ReadOnlyComposable get() = stringResource(statsByCategoryLabelRes)
        val noActiveVehicleTitle: String @Composable @ReadOnlyComposable get() = stringResource(noActiveVehicleTitleRes)
        val noActiveVehicleSubtitle: String @Composable @ReadOnlyComposable get() = stringResource(noActiveVehicleSubtitleRes)

        @StringRes val menuEditRes = R.string.costs_menu_edit
        @StringRes val menuDeleteRes = R.string.costs_menu_delete
        @StringRes val editSheetTitleRes = R.string.costs_edit_sheet_title
        @StringRes val updateButtonRes = R.string.costs_update_button
        @StringRes val deleteDialogTitleRes = R.string.costs_delete_dialog_title
        @StringRes val deleteDialogMessageRes = R.string.costs_delete_dialog_message
        @StringRes val deleteConfirmRes = R.string.costs_delete_confirm
        @StringRes val deleteCancelRes = R.string.costs_delete_cancel

        val menuEdit: String @Composable @ReadOnlyComposable get() = stringResource(menuEditRes)
        val menuDelete: String @Composable @ReadOnlyComposable get() = stringResource(menuDeleteRes)
        val editSheetTitle: String @Composable @ReadOnlyComposable get() = stringResource(editSheetTitleRes)
        val updateButton: String @Composable @ReadOnlyComposable get() = stringResource(updateButtonRes)
        val deleteDialogTitle: String @Composable @ReadOnlyComposable get() = stringResource(deleteDialogTitleRes)
        val deleteDialogMessage: String @Composable @ReadOnlyComposable get() = stringResource(deleteDialogMessageRes)
        val deleteConfirm: String @Composable @ReadOnlyComposable get() = stringResource(deleteConfirmRes)
        val deleteCancel: String @Composable @ReadOnlyComposable get() = stringResource(deleteCancelRes)

        @StringRes val periodMonthRes = R.string.costs_period_month
        @StringRes val period3MonthsRes = R.string.costs_period_3_months
        @StringRes val periodYearRes = R.string.costs_period_year
        @StringRes val periodAllRes = R.string.costs_period_all

        val periodMonth: String @Composable @ReadOnlyComposable get() = stringResource(periodMonthRes)
        val period3Months: String @Composable @ReadOnlyComposable get() = stringResource(period3MonthsRes)
        val periodYear: String @Composable @ReadOnlyComposable get() = stringResource(periodYearRes)
        val periodAll: String @Composable @ReadOnlyComposable get() = stringResource(periodAllRes)
    }

    object Profile {
        @StringRes val titleRes = R.string.profile_title
        @StringRes val editButtonRes = R.string.profile_edit_button
        @StringRes val vehiclesLabelRes = R.string.profile_vehicles_label
        @StringRes val enteredCostsLabelRes = R.string.profile_entered_costs_label
        @StringRes val accountInfoTitleRes = R.string.profile_account_info_title
        @StringRes val memberSinceLabelRes = R.string.profile_member_since_label
        @StringRes val planLabelRes = R.string.profile_plan_label
        @StringRes val settingsButtonDescriptionRes = R.string.profile_settings_button_description
        @StringRes val avatarDescriptionRes = R.string.profile_avatar_description

        val title: String @Composable @ReadOnlyComposable get() = stringResource(titleRes)
        val editButton: String @Composable @ReadOnlyComposable get() = stringResource(editButtonRes)
        val vehiclesLabel: String @Composable @ReadOnlyComposable get() = stringResource(vehiclesLabelRes)
        val enteredCostsLabel: String @Composable @ReadOnlyComposable get() = stringResource(enteredCostsLabelRes)
        val accountInfoTitle: String @Composable @ReadOnlyComposable get() = stringResource(accountInfoTitleRes)
        val memberSinceLabel: String @Composable @ReadOnlyComposable get() = stringResource(memberSinceLabelRes)
        val planLabel: String @Composable @ReadOnlyComposable get() = stringResource(planLabelRes)
        val settingsButtonDescription: String @Composable @ReadOnlyComposable get() = stringResource(settingsButtonDescriptionRes)
        val avatarDescription: String @Composable @ReadOnlyComposable get() = stringResource(avatarDescriptionRes)
    }

    object EditProfile {
        @StringRes val titleRes = R.string.edit_profile_title
        @StringRes val changeAvatarRes = R.string.edit_profile_change_avatar
        @StringRes val usernameLabelRes = R.string.edit_profile_username_label
        @StringRes val fullNameLabelRes = R.string.edit_profile_fullname_label
        @StringRes val emailLabelRes = R.string.edit_profile_email_label
        @StringRes val phoneLabelRes = R.string.edit_profile_phone_label
        @StringRes val saveButtonRes = R.string.edit_profile_save_button
        @StringRes val saveSuccessRes = R.string.edit_profile_save_success
        @StringRes val deleteAccountRes = R.string.edit_profile_delete_account
        @StringRes val deleteConfirmTitleRes = R.string.edit_profile_delete_confirm_title
        @StringRes val deleteConfirmMessageRes = R.string.edit_profile_delete_confirm_message
        @StringRes val deleteConfirmButtonRes = R.string.edit_profile_delete_confirm_button
        @StringRes val cancelRes = R.string.edit_profile_cancel
        @StringRes val emailReadonlyNoteRes = R.string.edit_profile_email_readonly_note
        @StringRes val avatarComingSoonRes = R.string.edit_profile_avatar_coming_soon
        @StringRes val sectionProfileRes = R.string.edit_profile_section_profile
        @StringRes val sectionContactRes = R.string.edit_profile_section_contact
        @StringRes val dangerZoneRes = R.string.edit_profile_danger_zone
        @StringRes val errorNameEmptyRes = R.string.edit_profile_error_name_empty

        val title: String @Composable @ReadOnlyComposable get() = stringResource(titleRes)
        val changeAvatar: String @Composable @ReadOnlyComposable get() = stringResource(changeAvatarRes)
        val usernameLabel: String @Composable @ReadOnlyComposable get() = stringResource(usernameLabelRes)
        val fullNameLabel: String @Composable @ReadOnlyComposable get() = stringResource(fullNameLabelRes)
        val emailLabel: String @Composable @ReadOnlyComposable get() = stringResource(emailLabelRes)
        val phoneLabel: String @Composable @ReadOnlyComposable get() = stringResource(phoneLabelRes)
        val saveButton: String @Composable @ReadOnlyComposable get() = stringResource(saveButtonRes)
        val saveSuccess: String @Composable @ReadOnlyComposable get() = stringResource(saveSuccessRes)
        val deleteAccount: String @Composable @ReadOnlyComposable get() = stringResource(deleteAccountRes)
        val deleteConfirmTitle: String @Composable @ReadOnlyComposable get() = stringResource(deleteConfirmTitleRes)
        val deleteConfirmMessage: String @Composable @ReadOnlyComposable get() = stringResource(deleteConfirmMessageRes)
        val deleteConfirmButton: String @Composable @ReadOnlyComposable get() = stringResource(deleteConfirmButtonRes)
        val cancel: String @Composable @ReadOnlyComposable get() = stringResource(cancelRes)
        val emailReadonlyNote: String @Composable @ReadOnlyComposable get() = stringResource(emailReadonlyNoteRes)
        val avatarComingSoon: String @Composable @ReadOnlyComposable get() = stringResource(avatarComingSoonRes)
        val sectionProfile: String @Composable @ReadOnlyComposable get() = stringResource(sectionProfileRes)
        val sectionContact: String @Composable @ReadOnlyComposable get() = stringResource(sectionContactRes)
        val dangerZone: String @Composable @ReadOnlyComposable get() = stringResource(dangerZoneRes)
        val errorNameEmpty: String @Composable @ReadOnlyComposable get() = stringResource(errorNameEmptyRes)
    }

    object Onboarding {
        @StringRes
        val skipRes = R.string.onboarding_skip

        @StringRes
        val nextRes = R.string.onboarding_next

        @StringRes
        val getStartedRes = R.string.onboarding_get_started

        @StringRes
        val welcomeTitleRes = R.string.onboarding_welcome_title

        @StringRes
        val welcomeSubtitleRes = R.string.onboarding_welcome_subtitle

        @StringRes
        val expensesTitleRes = R.string.onboarding_expenses_title

        @StringRes
        val expensesSubtitleRes = R.string.onboarding_expenses_subtitle

        @StringRes
        val aiTitleRes = R.string.onboarding_ai_title

        @StringRes
        val aiSubtitleRes = R.string.onboarding_ai_subtitle

        val skip: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(skipRes)

        val next: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(nextRes)

        val getStarted: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(getStartedRes)

        val welcomeTitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(welcomeTitleRes)

        val welcomeSubtitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(welcomeSubtitleRes)

        val expensesTitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(expensesTitleRes)

        val expensesSubtitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(expensesSubtitleRes)

        val aiTitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(aiTitleRes)

        val aiSubtitle: String
            @Composable
            @ReadOnlyComposable
            get() = stringResource(aiSubtitleRes)
    }
}