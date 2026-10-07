package aktual.budget.banksync.ui

import aktual.budget.banksync.domain.ProviderCredential
import aktual.budget.banksync.vm.providers.BankSyncProviderSetupState
import aktual.budget.banksync.vm.providers.BankSyncProviderSetupViewModel
import aktual.budget.banksync.vm.providers.SetupError
import aktual.budget.banksync.vm.providers.SetupField
import aktual.budget.model.AccountSyncSource
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Visibility
import aktual.core.icons.material.VisibilityOff
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PasswordTransformation
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PrimaryTextButtonWithLoading
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun BankSyncProviderSetupScreen(
  source: AccountSyncSource,
  back: BackNavigator,
  modifier: Modifier = Modifier,
  viewModel: BankSyncProviderSetupViewModel = bankSyncProviderSetupViewModel(source),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val uriHandler = LocalUriHandler.current

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        Saved -> back()
      }
    }
  }

  BankSyncProviderSetupScaffold(
    modifier = modifier,
    state = state,
    onAction = { action ->
      when (action) {
        CloseSetup -> back()
        SaveSetup -> viewModel.save()
        is SetCredential -> viewModel.setValue(action.credential, action.value)
        is OpenProviderSite -> uriHandler.openUri(action.url)
      }
    },
  )
}

@Composable
private fun bankSyncProviderSetupViewModel(source: AccountSyncSource) =
  assistedMetroViewModel<BankSyncProviderSetupViewModel, BankSyncProviderSetupViewModel.Factory>(
    key = source.value,
  ) {
    create(source)
  }

@Composable
private fun BankSyncProviderSetupScaffold(
  state: BankSyncProviderSetupState,
  onAction: BankSyncProviderSetupActionHandler,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = { onAction(CloseSetup) }) },
        title = { Text(text = Strings.bankSyncSetupTitle(providerName(state.source))) },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      BankSyncProviderSetupContent(
        modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
        state = state,
        scrollState = scrollState,
        contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
        onAction = onAction,
      )
    }
  }
}

@Composable
private fun BankSyncProviderSetupContent(
  state: BankSyncProviderSetupState,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  onAction: BankSyncProviderSetupActionHandler,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(contentPadding)
        .padding(BankSyncDS.settingsPadding),
    verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsFieldSpacing),
  ) {
    intro(state.source)?.let { intro ->
      Text(text = intro, style = typography.bodyMedium, color = colors.pageText)
    }

    providerSite(state.source)?.let { site ->
      NormalTextButton(
        modifier = Modifier.fillMaxWidth(),
        text = Strings.bankSyncSetupOpenSite(providerName(state.source)),
        onClick = { onAction(OpenProviderSite(site)) },
      )
    }

    state.redirectUrl?.let { url -> RedirectUrl(url) }

    for (field in state.fields) {
      CredentialField(
        field = field,
        isEnabled = !state.isSaving,
        onValueChange = { onAction(SetCredential(field.credential, it)) },
      )
    }

    state.error?.let { error ->
      Text(text = errorMessage(error), style = typography.bodyMedium, color = colors.errorText)
    }

    PrimaryTextButtonWithLoading(
      modifier = Modifier.fillMaxWidth(),
      text = Strings.bankSyncSetupSave,
      isLoading = state.isSaving,
      isEnabled = state.canSave,
      onClick = { onAction(SaveSetup) },
    )

    BottomSpacing()
  }
}

@Composable
private fun RedirectUrl(url: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsLabelSpacing),
  ) {
    Text(text = Strings.bankSyncSetupEnableBankingRedirect, style = typography.bodyMedium)
    SelectionContainer { Text(text = url, style = typography.bodyMedium, color = colors.pageText) }
    if (!url.startsWith("https://")) {
      Text(
        text = Strings.bankSyncSetupEnableBankingInsecure,
        style = typography.bodySmall,
        color = colors.warningText,
      )
    }
  }
}

@Composable
private fun CredentialField(
  field: SetupField,
  isEnabled: Boolean,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val textState = rememberTextFieldState(initialText = field.value)
  val isSecret = field.credential in SecretCredentials
  var isVisible by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(textState) {
    snapshotFlow { textState.text.toString() }.collect(onValueChange)
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(BankSyncDS.settingsLabelSpacing),
  ) {
    val label = credentialLabel(field.credential)
    Text(text = label, style = typography.labelLarge)
    AktualTextField(
      modifier = Modifier.fillMaxWidth(),
      state = textState,
      placeholderText = label,
      isEnabled = isEnabled,
      singleLine = field.credential != EnableBankingSecretKey,
      outputTransformation = if (isSecret && !isVisible) PasswordTransformation else null,
      keyboardOptions =
        KeyboardOptions(
          autoCorrectEnabled = false,
          capitalization = None,
          keyboardType = if (isSecret) KeyboardType.Password else KeyboardType.Text,
        ),
      trailingIcon =
        if (isSecret) {
          {
            BareIconButton(
              imageVector =
                if (isVisible) MaterialIcons.VisibilityOff else MaterialIcons.Visibility,
              contentDescription = label,
              onClick = { isVisible = !isVisible },
            )
          }
        } else {
          null
        },
    )
  }
}

// The Enable Banking key is a whole .pem file, so it's shown to check it pasted in properly
private val SecretCredentials =
  setOf(
    ProviderCredential.GoCardlessSecretKey,
    ProviderCredential.SimpleFinToken,
    ProviderCredential.PluggyAiClientSecret,
    ProviderCredential.AkahuAppToken,
    ProviderCredential.AkahuUserToken,
  )

@Composable
private fun credentialLabel(credential: ProviderCredential): String =
  when (credential) {
    GoCardlessSecretId -> Strings.bankSyncSetupGocardlessSecretId
    GoCardlessSecretKey -> Strings.bankSyncSetupGocardlessSecretKey
    SimpleFinToken -> Strings.bankSyncSetupSimplefinToken
    PluggyAiClientId -> Strings.bankSyncSetupPluggyaiClientId
    PluggyAiClientSecret -> Strings.bankSyncSetupPluggyaiClientSecret
    PluggyAiItemIds -> Strings.bankSyncSetupPluggyaiItemIds
    AkahuAppToken -> Strings.bankSyncSetupAkahuAppToken
    AkahuUserToken -> Strings.bankSyncSetupAkahuUserToken
    EnableBankingApplicationId -> Strings.bankSyncSetupEnableBankingApplicationId
    EnableBankingSecretKey -> Strings.bankSyncSetupEnableBankingSecretKey
  }

@Composable
private fun intro(source: AccountSyncSource): String? =
  when (source) {
    GoCardless -> Strings.bankSyncSetupGocardlessIntro
    SimpleFin -> Strings.bankSyncSetupSimplefinIntro
    PluggyAi -> Strings.bankSyncSetupPluggyaiIntro
    Akahu -> Strings.bankSyncSetupAkahuIntro
    EnableBanking -> Strings.bankSyncSetupEnableBankingIntro
    else -> null
  }

// Where each provider's initialise modal sends the user to get their credentials
private fun providerSite(source: AccountSyncSource): String? =
  when (source) {
    GoCardless -> "https://bankaccountdata.gocardless.com/overview/"
    SimpleFin -> "https://beta-bridge.simplefin.org/"
    PluggyAi -> "https://dashboard.pluggy.ai/"
    Akahu -> "https://my.akahu.nz/developers"
    EnableBanking -> "https://enablebanking.com/cp/applications"
    else -> null
  }

@Composable
private fun errorMessage(error: SetupError): String =
  when (error) {
    NotAdmin -> Strings.bankSyncSetupNotAdmin
    LoggedOut -> Strings.bankSyncSetupLoggedOut
    is Other -> error.cause ?: Strings.bankSyncSetupFailed
  }

@PortraitPreview
@Composable
private fun PreviewBankSyncProviderSetupScaffold(
  @PreviewParameter(BankSyncProviderSetupStateProvider::class)
  params: ColoredParams<BankSyncProviderSetupState>,
) = PreviewWithColoredParams(params) { BankSyncProviderSetupScaffold(state = this, onAction = {}) }

private fun previewState(
  source: AccountSyncSource,
  redirectUrl: String? = null,
  isSaving: Boolean = false,
  error: SetupError? = null,
) =
  BankSyncProviderSetupState(
    source = source,
    fields =
      ProviderCredential.of(source)
        .mapIndexed { i, credential -> SetupField(credential, if (i == 0) "abc-123" else "") }
        .toImmutableList(),
    redirectUrl = redirectUrl,
    isSaving = isSaving,
    error = error,
  )

private class BankSyncProviderSetupStateProvider :
  ColoredParameterProvider<BankSyncProviderSetupState>(
    previewState(GoCardless),
    previewState(GoCardless, isSaving = true),
    previewState(PluggyAi, error = NotAdmin),
    previewState(
      EnableBanking,
      redirectUrl = "http://192.168.1.10:5006/enablebanking/auth_callback",
    ),
  )
