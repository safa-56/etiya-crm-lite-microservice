/* =============================================================================
 * Etiya CRM Lite - login formu alan kisitlari
 * -----------------------------------------------------------------------------
 * Keycloak login temasi tarafindan tum login sayfalarina enjekte edilir
 * (bkz. theme.properties -> scripts=js/field-limits.js).
 *
 * Karsiladigi kabul kriterleri:
 *   AC-1: Kullanici adi alani en fazla 50 karakter kabul eder; sinir asildiginda
 *         tarayici fazladan karakter girisine izin vermez (maxlength=50).
 *   AC-4: Sifre alani en fazla 50 karakter kabul eder (maxlength=50).
 *   AC-2: Kullanici adinin basindaki/sonundaki bosluklar gonderim aninda
 *         otomatik temizlenir (trim).
 *
 * Not: Bu istemci tarafi kisitlar UX icindir. Sunucu tarafinda ayni sinirlar
 *      realm ayarlariyla (declarative user profile length(max=50), passwordPolicy
 *      maxLength(50)) da zorlanir; iki katman birlikte calisir.
 * ========================================================================== */
(function () {
  "use strict";

  var MAX_LEN = 50;

  function applyLimits() {
    var username = document.getElementById("username");
    var password = document.getElementById("password");

    // AC-1 / AC-4: girisin fazla karakter kabul etmemesi icin maxlength.
    if (username) {
      username.setAttribute("maxlength", String(MAX_LEN));
    }
    if (password) {
      password.setAttribute("maxlength", String(MAX_LEN));
    }

    // AC-2: kullanici adinin bas/son bosluklarini gonderimden hemen once kirp.
    var form = (username && username.form) || document.getElementById("kc-form-login");
    if (form && username && !form.dataset.etiyaTrimBound) {
      form.dataset.etiyaTrimBound = "true";
      form.addEventListener("submit", function () {
        username.value = username.value.trim();
      });
    }
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", applyLimits);
  } else {
    applyLimits();
  }
})();
