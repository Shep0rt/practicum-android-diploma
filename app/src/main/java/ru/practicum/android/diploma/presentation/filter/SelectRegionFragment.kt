package ru.practicum.android.diploma.presentation.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.domain.models.Region
import ru.practicum.android.diploma.presentation.ui.theme.AppTheme
import ru.practicum.android.diploma.presentation.ui.theme.Blue
import ru.practicum.android.diploma.presentation.ui.theme.Dimens

class SelectRegionFragment : Fragment() {

    private val viewModel: SelectRegionViewModel by viewModel()

    override fun onCreateView(
        鏡inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    SelectRegionScreen(
                        viewModel = viewModel,
                        onRegionClicked = { region ->
                            // Формируем Bundle для возврата на экран Место работы
                            val result = Bundle().apply {
                                putString(REG_ID, region.id)
                                putString(REG_NAME, region.name)
                                // Критерий автоопределения страны: передаем ID и Name страны,
                                // которые ты уже успешно замапил в репозитории внутри Region!
                                putString(CTR_ID, region.countryId)
                                putString(CTR_NAME, region.countryName)
                            }
                            // Отправляем результат по ключу
                            parentFragmentManager.setFragmentResult(REGION_RESULT_KEY, result)
                            // Возвращаемся назад
                            findNavController().popBackStack()
                        },
                        onBackClicked = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }

    companion object {
        const val COUNTRY_ID_KEY = "country_id_key"

        // Ключи для передачи данных между фрагментами
        const val REGION_RESULT_KEY = "region_result_key"
        const val REG_ID = "reg_id"
        const val REG_NAME = "reg_name"
        const val CTR_ID = "ctr_id"
        const val CTR_NAME = "ctr_name"
    }
}

@Composable
fun SelectRegionScreen(
    viewModel: SelectRegionViewModel,
    onRegionClicked: (Region) -> Unit,
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Извлекаем текущий поисковый запрос из стейта, который написал твой коллега
    val searchQuery = when (val state = uiState) {
        is SelectRegionUiState.Content -> state.query
        is SelectRegionUiState.EmptySearch -> state.query
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Тулбар экрана
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_24), // Укажи свой ID стрелки назад
                    contentDescription = "Назад"
                )
            }
            Text(
                text = stringResource(R.string.filter_region_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier.padding(start = Dimens.paddingSystemBar)
            )
        }

        // Поле ввода поискового запроса (Критерий: мгновенный поиск на лету)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onQueryChanged(it) },
            placeholder = { 
                Text(
                    text = stringResource(R.string.filter_region_hint),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                ) 
            }, // Наш Hint (подсказка)
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.paddingDefault, vertical = Dimens.paddingSystemBar),
            singleLine = true,
            shape = RoundedCornerShape(Dimens.cornerRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                cursorColor = Blue,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            ),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onClearQueryClicked() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close_24), // Твой крестик для очистки
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_search_24), // Иконка лупы, если пусто
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )

        // Контентная часть в зависимости от стейта
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is SelectRegionUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                is SelectRegionUiState.Error -> {
                    // Плейсхолдер ошибки загрузки из сети
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.il_region_error_328), // Твоя картинка ошибки
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.height(Dimens.paddingDefault))
                        Text(
                            text = stringResource(R.string.filter_region_error),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp)
                        )
                    }
                }
                is SelectRegionUiState.EmptySearch -> {
                    // Плейсхолдер "Такого региона нет"
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.il_main_no_results_328), // Твоя картинка "нет результатов"
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.height(Dimens.paddingDefault))
                        Text(
                            text = stringResource(R.string.filter_region_empty),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp)
                        )
                    }
                }
                is SelectRegionUiState.Content -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.regions) { region ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimens.heightMedium)
                                    .clickable { onRegionClicked(region) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = region.name,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                )
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_forward_24),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
