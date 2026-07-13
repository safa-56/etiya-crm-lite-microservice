/* =============================================================================
 * Etiya CRM Lite - login ekrani hata mesaji davranislari
 * -----------------------------------------------------------------------------
 * Keycloak login temasi tarafindan tum login sayfalarina enjekte edilir
 * (bkz. theme.properties -> scripts).
 *
 * Karsiladigi kabul kriterleri:
 *   - Kullanici adi veya sifre hatali oldugunda hata mesaji ("Wrong user name
 *     or password. Please try again.") kullanici adi alaninin UZERINDE
 *     gosterilir. Mesaj metni: messages_en.properties -> invalidUserMessage.
 *   - Hesap brute-force ile kilitlendiginde kilit mesaji ("Your account has
 *     been locked. Please try again after 15 minutes.") gosterilir ve Login
 *     butonu PASIF kalir. Mesaj metni: messages_en.properties ->
 *     accountTemporarilyDisabledMessage.
 *
 * NOT: Mesaj METINLERI messages_en.properties icinde tanimlidir. Asagidaki
 *      LOCKED_MESSAGE sabiti o dosyadaki 'accountTemporarilyDisabledMessage'
 *      degeriyle birebir ayni olmalidir.
 * ========================================================================== */
(function () {
  "use strict";

  // messages_en.properties -> accountTemporarilyDisabledMessage ile birebir ayni.
  var LOCKED_MESSAGE =
    "Your account has been locked. Please try again after 15 minutes.";

  // Base (classic) keycloak temasi hata mesajini 'alert-error' kutusunda,
  // metni de '.kc-feedback-text' icinde render eder.
  function findErrorAlert() {
    return document.querySelector(
      ".alert-error, .pf-c-alert.pf-m-danger, .pf-c-alert.pf-m-error"
    );
  }

  function alertText(alertEl) {
    if (!alertEl) return "";
    var textEl = alertEl.querySelector(".kc-feedback-text") || alertEl;
    return (textEl.textContent || "").trim();
  }

  // Hata mesajini kullanici adi alaninin hemen ustune tasi.
  function moveAlertAboveUsername(alertEl) {
    var username = document.getElementById("username");
    if (!alertEl || !username) return;
    var group =
      username.closest(".kc-form-group, .pf-c-form__group, .form-group") ||
      username.parentNode;
    if (group && group.parentNode) {
      group.parentNode.insertBefore(alertEl, group);
    }
  }

  function disableLoginButton() {
    var btn = document.getElementById("kc-login");
    if (!btn) return;
    btn.disabled = true;
    btn.setAttribute("aria-disabled", "true");
    btn.classList.add("disabled");
  }

  function apply() {
    var alertEl = findErrorAlert();
    if (!alertEl) return;

    // Her hata durumunda: mesaji kullanici adi alaninin ustune konumla.
    moveAlertAboveUsername(alertEl);

    // Yalnizca hesap kilitliyken: Login butonunu pasiflestir.
    if (alertText(alertEl).indexOf(LOCKED_MESSAGE) !== -1) {
      disableLoginButton();
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", apply);
  } else {
    apply();
  }
})();
