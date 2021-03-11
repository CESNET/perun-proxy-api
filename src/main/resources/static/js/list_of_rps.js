const showOidc = $('#showOidc').attr('content');
const showSaml = $('#showSaml').attr('content');
const showTesting = $('#showTesting').attr('content');
const showStaging = $('#showStaging').attr('content');
const showProduction = $('#showProduction').attr('content');
const labels = [];
const data = [];
const backgroundColor = [];
const borderColor = [];
let jsonData = null;

function getData(dataKey) {
    if (jsonData === null) {
        jsonData = JSON.parse($('#dataStats').attr('content'));
    }
    return jsonData[dataKey];
}

function initializeChartData() {
    if (showSaml) {
        if (showProduction) {
            data.push(getData('samlProductionCount'));
            labels.push($('#productionServicesSaml').attr('content'));
            backgroundColor.push('rgba(50, 0, 255, 0.2)');
            borderColor.push('rgba(50, 0, 252, 1)');
        }
        if (showStaging) {
            data.push(getData('samlStagingCount'));
            labels.push($('#stageServicesSaml').attr('content'));
            backgroundColor.push('rgba(50, 150, 255, 0.2)');
            borderColor.push('rgba(50, 150, 252, 1)');
        }
        if (showTesting) {
            data.push(getData('samlTestingCount'));
            labels.push($('#testServicesSaml').attr('content'));
            backgroundColor.push('rgba(50, 220, 255, 0.2)');
            borderColor.push('rgba(50, 220, 252, 1)');
        }
    }
    if (showOidc) {
        if (showProduction) {
            data.push(getData('oidcProductionCount'));
            labels.push($('#productionServicesOidc').attr('content'));
            backgroundColor.push('rgba(0, 255, 50, 0.2)');
            borderColor.push('rgba(0, 255, 50, 1)');
        }
        if (showStaging) {
            data.push(getData('oidcStagingCount'));
            labels.push($('#stageServicesOidc').attr('content'));
            backgroundColor.push('rgba(150, 255, 50, 0.2)');
            borderColor.push('rgba(150, 255, 50, 1)');
        }
        if (showTesting) {
            data.push(getData('oidcTestingCount'));
            labels.push($('#testServicesOidc').attr('content'));
            backgroundColor.push('rgba(220, 255, 50, 0.2)');
            borderColor.push('rgba(220, 255, 50, 1)');
        }
    }
}

initializeChartData();

const ctx = document.getElementById("myChart").getContext('2d');
new Chart(ctx, {
    type: 'bar',
    data: {
        labels: labels,
        datasets: [{
            label: '',
            data: data,
            backgroundColor: backgroundColor,
            borderColor: borderColor,
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
Chart.platform.disableCSSInjection = true;