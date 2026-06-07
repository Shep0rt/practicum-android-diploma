package ru.practicum.android.diploma.presentation.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.presentation.ui.theme.AppTheme
import ru.practicum.android.diploma.presentation.ui.theme.Dimens

class WorkplaceFragment : Fragment() {

    private val viewModel: WorkplaceViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Слушаем возврат с экрана выбора страны
        parentFragmentManager.setFragmentResultListener(
            COUNTRY_SELECTION_REQUEST_KEY,
            this
        ) { _, bundle ->
            val countryId = bundle.getString(COUNTRY_ID_KEY) ?: ""
            val countryName = bundle.getString(COUNTRY_NAME_KEY) ?: ""
            viewModel.onCountrySelected(countryId, countryName)
        }

        // Слушаем возврат с экрана выбора региона (Выполняет Задача 4.2 - автоопределение страны)
        parentFragmentManager.setFragmentResultListener(
            SelectRegionFragment.REGION_RESULT_KEY,
            this
        ) { _, bundle ->
            val regId = bundle.getString(SelectRegionFragment.REG_ID) ?: ""
            val regName = bundle.getString(SelectRegionFragment.REG_NAME) ?: ""
            val ctrId = bundle.getString(SelectRegionFragment.CTR_ID) ?: ""
            val ctrName = bundle.getString(SelectRegionFragment.CTR_NAME) ?: ""

            // Твой готовый метод во ViewModel, который идеально обновляет стейт и прописывает авто-страну!
            viewModel.onRegionSelected(
                regionId = regId,
                regionName = regName,
                countryId = ctrId,
                countryName = ctrName
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    WorkplaceScreen(
                        viewModel = viewModel,
                        onNavigateToCountry = {
                            findNavController().navigate(R.id.action_workplaceFragment_to_selectCountryFragment)
                        },
                        onNavigateToRegion = {
                            // Передаем текущую страну (если она есть) на экран региона, чтобы отфильтровать список
                            val currentCountryId = viewModel.uiState.value.countryId
                            val args = Bundle().apply {
                                putString(SelectRegionFragment.COUNTRY_ID_KEY, currentCountryId)
                            }
                            findNavController().navigate(R.id.action_workplaceFragment_to_selectRegionFragment, args)
                        },
                        onBackClicked = { findNavController().popBackStack() },
                        onApplyAndExit = {
                            // Вызываем твой рабочий метод окончательного сохранения в постоянный фильтр (Задача 4.3)
                            viewModel.onApplyClicked()
                            findNavController().popBackStack() // Возврат на главный экран Фильтр
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val COUNTRY_ID_KEY = "country_id_key"
        const val COUNTRY_NAME_KEY = "country_name_key"
        const val COUNTRY_SELECTION_REQUEST_KEY = "country_selection_request_key"
    }
}

@Composable
fun WorkplaceScreen(
    viewModel: WorkplaceViewModel,
    onNavigateToCountry: () -> Unit,
    onNavigateToRegion: () -> Unit,
    onBackClicked: () -> Unit,
    onApplyAndExit: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Задача 4.3: Кнопка "Выбрать" видна только если стейт изменился
    val isApplyButtonVisible = state.showApplyButton

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.paddingDefault, vertical = Dimens.paddingDefault),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Тулбар
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.paddingSystemBar),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClicked, modifier = Modifier.offset(x = -Dimens.paddingMedium)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.filter_workplace_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(Dimens.paddingExtraLarge))

            // Поле выбора Страны
            WorkplaceInputField(
                label = stringResource(R.string.filter_country),
                value = state.countryName,
                onFieldClick = onNavigateToCountry,
                onClearClick = { viewModel.onCountryCleared() }
            )

            Spacer(modifier = Modifier.height(Dimens.paddingDefault))

            // Поле выбора Региона
            WorkplaceInputField(
                label = stringResource(R.string.filter_region),
                value = state.regionName,
                onFieldClick = onNavigateToRegion,
                onClearClick = { viewModel.onRegionCleared() }
            )
        }

        // Задача 4.3: Кнопка "Выбрать" видна только если стейт изменился
        val isApplyButtonVisible = state.showApplyButton

        // Задача 4.3: Нижняя кнопка подтверждения выбора
        if (isApplyButtonVisible) {
            Button(
                onClick = onApplyAndExit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.heightButton),
                shape = RoundedCornerShape(Dimens.cornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ru.practicum.android.diploma.presentation.ui.theme.Blue,
                    contentColor = ru.practicum.android.diploma.presentation.ui.theme.WhiteUniversal
                )
            ) {
                Text(
                    text = stringResource(R.string.filter_workplace_apply),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun WorkplaceInputField(
    label: String,
    value: String?,
    onFieldClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.heightMedium)
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(Dimens.cornerRadius))
            .clickable { onFieldClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (value != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (value != null) {
            IconButton(onClick = onClearClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_close_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
