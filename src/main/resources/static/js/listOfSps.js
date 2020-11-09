Chart.platform.disableCSSInjection = true;

const ctx = document.getElementById("myChart").getContext('2d');
new Chart(ctx, { // eslint-disable-line no-new
    type: 'bar',
    data: {
        labels: [
            labelsMap['production_saml'],
            labelsMap['staging_saml'],
            labelsMap['testing_saml'],
            labelsMap['production_oidc'],
            labelsMap['staging_oidc'],
            labelsMap['testing_oidc'],
        ],
        datasets: [{
            label: '',
            data: [
                JSON.parse(document.getElementById("dataStats").getAttribute('content'))['samlProductionServicesCount'],
                JSON.parse(document.getElementById("dataStats").getAttribute('content'))['samlStagingServicesCount'],
                JSON.parse(document.getElementById("dataStats").getAttribute('content'))['samlTestingServicesCount'],
                JSON.parse(document.getElementById("dataStats").getAttribute('content'))['oidcProductionServicesCount'],
                JSON.parse(document.getElementById("dataStats").getAttribute('content'))['oidcStagingServicesCount'],
                JSON.parse(document.getElementById("dataStats").getAttribute('content'))['oidcTestingServicesCount']
            ],
            backgroundColor: [
                'rgba(50, 0, 255, 0.2)',
                'rgba(50, 150, 255, 0.2)',
                'rgba(50, 220, 255, 0.2)',
                'rgba(0, 255, 50, 0.2)',
                'rgba(150, 255, 50, 0.2)',
                'rgba(220, 255, 50, 0.2)'
            ],
            borderColor: [
                'rgba(50, 0, 252, 1)',
                'rgba(50, 150, 252, 1)',
                'rgba(50, 220, 252, 1)',
                'rgba(0, 255, 50, 1)',
                'rgba(150, 255, 50, 1)',
                'rgba(220, 255, 50, 1)'
            ],
            borderWidth: 1
        }]
    },
    options: {
        scales: {
            yAxes: [{
                ticks: {
                    beginAtZero: true,
                    callback: function (value) {
                        if (Number.isInteger(value)) {
                            return value;
                        }
                    }
                }
            }]
        },
        legend: {
            display: false
        },
        tooltips: {
            callbacks: {
                label: function (tooltipItem) {
                    return tooltipItem.yLabel;
                }
            }
        }
    }
});