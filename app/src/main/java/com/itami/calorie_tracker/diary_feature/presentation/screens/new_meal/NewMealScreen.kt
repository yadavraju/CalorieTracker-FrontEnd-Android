package com.itami.calorie_tracker.diary_feature.presentation.screens.new_meal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.itami.calorie_tracker.R
import com.itami.calorie_tracker.core.domain.model.Theme
import com.itami.calorie_tracker.core.presentation.components.DialogComponent
import com.itami.calorie_tracker.core.presentation.components.NutrientAmountItem
import com.itami.calorie_tracker.core.presentation.components.ObserveAsEvents
import com.itami.calorie_tracker.core.presentation.navigation.util.NavResultCallback
import com.itami.calorie_tracker.core.presentation.theme.CalorieTrackerTheme
import com.itami.calorie_tracker.diary_feature.domain.model.ConsumedFood
import com.itami.calorie_tracker.diary_feature.presentation.components.ConsumedFoodComponent
import com.itami.calorie_tracker.diary_feature.presentation.components.ConsumedFoodDialog
import kotlin.math.roundToInt

@Composable
fun NewMealScreen(
    onNavigateSearchFood: (NavResultCallback<ConsumedFood?>) -> Unit,
    onMealSaved: () -> Unit,
    onNavigateBack: () -> Unit,
    onShowSnackbar: (message: String) -> Unit,
    viewModel: NewMealViewModel = hiltViewModel(),
) {
    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is NewMealUiEvent.MealSaved -> onMealSaved()
            is NewMealUiEvent.ShowSnackbar -> onShowSnackbar(event.message)
            is NewMealUiEvent.NavigateBack -> onNavigateBack()
            is NewMealUiEvent.NavigateToSearchFood -> onNavigateSearchFood { consumedFoodResult ->
                consumedFoodResult?.let { consumedFood ->
                    viewModel.onAction(NewMealAction.AddConsumedFoodRequest(consumedFood))
                }
            }
        }
    }

    NewMealScreenContent(
        state = viewModel.state,
        onAction = viewModel::onAction
    )
}

@Preview
@Composable
fun NewMealScreenContentPreview() {
    CalorieTrackerTheme(theme = Theme.SYSTEM_THEME) {
        NewMealScreenContent(
            state = NewMealState(),
            onAction = {}
        )
    }
}

@Composable
private fun NewMealScreenContent(
    state: NewMealState,
    onAction: (action: NewMealAction) -> Unit,
) {
    BackHandler {
        onAction(NewMealAction.NavigateBackClick)
    }

    if (state.showExitDialog) {
        DialogComponent(
            title = stringResource(R.string.exit),
            description = stringResource(R.string.exit_warning),
            cancelText = stringResource(R.string.cancel),
            confirmText = stringResource(R.string.exit),
            onConfirm = {
                onAction(NewMealAction.NavigateBackConfirmClick)
            },
            onDismiss = {
                onAction(NewMealAction.NavigateBackDenyClick)
            }
        )
    }

    if (state.selectedConsumedFoodIndex != null) {
        val index = state.selectedConsumedFoodIndex
        state.consumedFoods.getOrNull(index)?.let { consumedFood ->
            ConsumedFoodDialog(
                consumedFood = consumedFood,
                onConfirm = { weightGrams ->
                    onAction(
                        NewMealAction.UpdateConsumedFood(
                            index = index,
                            weightGrams = weightGrams
                        )
                    )
                },
                onDismiss = {
                    onAction(NewMealAction.SelectConsumedFood(null))
                }
            )
        }
    }

    Scaffold(
        containerColor = CalorieTrackerTheme.colors.background,
        contentColor = CalorieTrackerTheme.colors.onBackground,
        topBar = {
            TopBarSection(
                mealName = state.mealName,
                onMealNameChange = { newValue ->
                    onAction(NewMealAction.MealNameChange(newValue))
                },
                consumedFoods = state.consumedFoods,
                onCloseIconClick = {
                    onAction(NewMealAction.NavigateBackClick)
                },
                onAddFoodIconClick = {
                    onAction(NewMealAction.AddFoodIconClick)
                },
            )
        },
        bottomBar = {
            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = CalorieTrackerTheme.padding.default,
                        end = CalorieTrackerTheme.padding.default,
                        bottom = CalorieTrackerTheme.padding.large
                    ),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = CalorieTrackerTheme.colors.primary,
                    contentColor = CalorieTrackerTheme.colors.onPrimary
                ),
                shape = CalorieTrackerTheme.shapes.small,
                contentPadding = PaddingValues(
                    vertical = CalorieTrackerTheme.padding.default
                ),
                onClick = {
                    onAction(NewMealAction.SaveMealClick)
                },
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = CalorieTrackerTheme.typography.titleSmall,
                    color = CalorieTrackerTheme.colors.onPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.Center
        ) {
            ConsumedFoodsSection(
                modifier = Modifier
                    .padding(top = CalorieTrackerTheme.padding.extraSmall)
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                consumedFoods = state.consumedFoods,
                onConsumedFoodClick = { index ->
                    onAction(NewMealAction.SelectConsumedFood(index))
                },
                onEditConsumedFood = { index ->
                    onAction(NewMealAction.SelectConsumedFood(index))
                },
                onDeleteConsumedFood = { index ->
                    onAction(NewMealAction.DeleteConsumedFood(index))
                }
            )
            if (state.isLoading) {
                CircularProgressIndicator(color = CalorieTrackerTheme.colors.primary)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConsumedFoodsSection(
    modifier: Modifier,
    consumedFoods: List<ConsumedFood>,
    onConsumedFoodClick: (index: Int) -> Unit,
    onEditConsumedFood: (index: Int) -> Unit,
    onDeleteConsumedFood: (index: Int) -> Unit,
) {
    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(items = consumedFoods) { index, consumedFood ->
            ConsumedFoodComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement(),
                consumedFood = consumedFood,
                onClick = {
                    onConsumedFoodClick(index)
                },
                onEdit = {
                    onEditConsumedFood(index)
                },
                onDelete = {
                    onDeleteConsumedFood(index)
                }
            )
        }
    }
}

@Composable
private fun TopBarSection(
    onCloseIconClick: () -> Unit,
    onAddFoodIconClick: () -> Unit,
    mealName: String,
    onMealNameChange: (String) -> Unit,
    consumedFoods: List<ConsumedFood>,
) {
    val calories = remember(consumedFoods) {
        consumedFoods.sumOf { (it.grams / 100f * it.food.caloriesIn100Grams).roundToInt() }
    }

    val proteins = remember(consumedFoods) {
        consumedFoods.sumOf { (it.grams / 100f * it.food.proteinsIn100Grams).roundToInt() }
    }

    val fats = remember(consumedFoods) {
        consumedFoods.sumOf { (it.grams / 100f * it.food.fatsIn100Grams).roundToInt() }
    }

    val carbs = remember(consumedFoods) {
        consumedFoods.sumOf { (it.grams / 100f * it.food.carbsIn100Grams).roundToInt() }
    }

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CalorieTrackerTheme.padding.default),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                modifier = Modifier.weight(weight = 0.3f, fill = true),
                onClick = onCloseIconClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_close),
                    contentDescription = stringResource(R.string.desc_icon_close),
                    tint = CalorieTrackerTheme.colors.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = true),
                value = mealName,
                onValueChange = onMealNameChange,
                textStyle = CalorieTrackerTheme.typography.titleSmall.copy(textAlign = TextAlign.Center),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = CalorieTrackerTheme.colors.onBackground,
                    unfocusedTextColor = CalorieTrackerTheme.colors.onBackground,
                    focusedPlaceholderColor = CalorieTrackerTheme.colors.onBackgroundVariant,
                    unfocusedPlaceholderColor = CalorieTrackerTheme.colors.onBackgroundVariant,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = CalorieTrackerTheme.colors.primary
                ),
                maxLines = 1,
                placeholder = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = stringResource(R.string.hint_meal_name),
                        style = CalorieTrackerTheme.typography.bodyLarge,
                        color = CalorieTrackerTheme.colors.onBackgroundVariant,
                        textAlign = TextAlign.Center
                    )
                },
            )
            IconButton(
                modifier = Modifier.weight(weight = 0.3f, fill = true),
                onClick = onAddFoodIconClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_add),
                    contentDescription = stringResource(R.string.desc_icon_add),
                    tint = CalorieTrackerTheme.colors.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(CalorieTrackerTheme.spacing.small))
        Row(
            modifier = Modifier
                .padding(horizontal = CalorieTrackerTheme.padding.default)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NutrientAmountItem(
                modifier = Modifier.weight(1f),
                nutrientName = stringResource(id = R.string.nutrient_name_calories),
                nutrientAmount = calories.toString()
            )
            NutrientAmountItem(
                modifier = Modifier.weight(1f),
                nutrientName = stringResource(id = R.string.nutrient_name_proteins),
                nutrientAmount = stringResource(id = R.string.amount_grams, proteins)
            )
            NutrientAmountItem(
                modifier = Modifier.weight(1f),
                nutrientName = stringResource(id = R.string.nutrient_name_fats),
                nutrientAmount = stringResource(id = R.string.amount_grams, fats)
            )
            NutrientAmountItem(
                modifier = Modifier.weight(1f),
                nutrientName = stringResource(id = R.string.nutrient_name_carbs),
                nutrientAmount = stringResource(id = R.string.amount_grams, carbs)
            )
        }
        Spacer(modifier = Modifier.height(CalorieTrackerTheme.spacing.default))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = CalorieTrackerTheme.colors.outline
        )
    }
}
