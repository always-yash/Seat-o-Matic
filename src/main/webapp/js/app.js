/**
 * College Lost & Found Portal - Frontend Interactions
 */

document.addEventListener("DOMContentLoaded", function () {
    // 1. Auto-dismiss flash alerts after 5 seconds
    const alerts = document.querySelectorAll(".alert-dismissible");
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });

    // 2. Image Upload Preview
    const imageInput = document.getElementById("imageFileInput");
    const previewContainer = document.getElementById("imagePreviewContainer");
    const previewImg = document.getElementById("imagePreview");

    if (imageInput && previewContainer && previewImg) {
        imageInput.addEventListener("change", function () {
            const file = this.files[0];
            if (file) {
                // Check 5MB limit on client side
                if (file.size > 5 * 1024 * 1024) {
                    alert("Selected image is larger than 5 MB. Please choose a smaller image.");
                    this.value = "";
                    previewContainer.classList.add("d-none");
                    return;
                }
                const reader = new FileReader();
                reader.onload = function (e) {
                    previewImg.src = e.target.result;
                    previewContainer.classList.remove("d-none");
                };
                reader.readAsDataURL(file);
            } else {
                previewContainer.classList.add("d-none");
            }
        });
    }

    // 3. Geolocation auto-fill button
    const geoBtn = document.getElementById("btnGetCoordinates");
    const latInput = document.getElementById("latitudeInput");
    const lonInput = document.getElementById("longitudeInput");

    if (geoBtn && latInput && lonInput) {
        geoBtn.addEventListener("click", function () {
            if ("geolocation" in navigator) {
                geoBtn.disabled = true;
                geoBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Locating...';

                navigator.geolocation.getCurrentPosition(
                    function (pos) {
                        latInput.value = pos.coords.latitude.toFixed(6);
                        lonInput.value = pos.coords.longitude.toFixed(6);
                        geoBtn.disabled = false;
                        geoBtn.innerHTML = '<i class="bi bi-geo-alt-fill"></i> Coordinates Found!';
                        setTimeout(function () {
                            geoBtn.innerHTML = '<i class="bi bi-geo-alt"></i> Use Current GPS';
                        }, 3000);
                    },
                    function (err) {
                        alert("Could not retrieve GPS location: " + err.message + ". You may enter coordinates manually or leave them default.");
                        geoBtn.disabled = false;
                        geoBtn.innerHTML = '<i class="bi bi-geo-alt"></i> Use Current GPS';
                    },
                    { timeout: 8000 }
                );
            } else {
                alert("Geolocation is not supported by your browser.");
            }
        });
    }

    // 4. Password confirmation validation
    const registerForm = document.getElementById("registerForm");
    if (registerForm) {
        registerForm.addEventListener("submit", function (e) {
            const p1 = document.getElementById("regPassword").value;
            const p2 = document.getElementById("regConfirmPassword").value;
            if (p1 !== p2) {
                e.preventDefault();
                alert("Passwords do not match. Please verify.");
            }
        });
    }
});
