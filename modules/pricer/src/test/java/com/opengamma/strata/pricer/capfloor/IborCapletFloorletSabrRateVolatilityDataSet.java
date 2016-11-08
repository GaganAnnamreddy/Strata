/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.capfloor;

import static com.opengamma.strata.basics.currency.Currency.EUR;
import static com.opengamma.strata.basics.date.DayCounts.ACT_ACT_ISDA;
import static com.opengamma.strata.basics.index.IborIndices.EUR_EURIBOR_3M;
import static com.opengamma.strata.market.curve.interpolator.CurveInterpolators.LINEAR;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.opengamma.strata.collect.array.DoubleArray;
import com.opengamma.strata.market.ValueType;
import com.opengamma.strata.market.curve.CurveMetadata;
import com.opengamma.strata.market.curve.CurveName;
import com.opengamma.strata.market.curve.Curves;
import com.opengamma.strata.market.curve.DefaultCurveMetadata;
import com.opengamma.strata.market.curve.InterpolatedNodalCurve;
import com.opengamma.strata.market.curve.SimpleCurveParameterMetadata;
import com.opengamma.strata.market.param.ParameterMetadata;
import com.opengamma.strata.pricer.model.SabrParameters;
import com.opengamma.strata.pricer.model.SabrVolatilityFormula;
import com.opengamma.strata.pricer.rate.ImmutableRatesProvider;

/**
 * Data sets for testing SABR model for Ibor caplet/floorlet.
 */
public class IborCapletFloorletSabrRateVolatilityDataSet {

  private static final DoubleArray EUR_DSC_TIME = DoubleArray.of(0.0, 0.5, 1.0, 2.0, 5.0, 10.0);
  private static final DoubleArray EUR_DSC_RATE = DoubleArray.of(0.0150, 0.0125, 0.0150, 0.0175, 0.0150, 0.0150);
  private static final CurveName EUR_DSC_NAME = CurveName.of("EUR-DSCON");
  private static final CurveMetadata EUR_DSC_META = Curves.zeroRates(EUR_DSC_NAME, ACT_ACT_ISDA);
  private static final InterpolatedNodalCurve EUR_DSC_CURVE =
      InterpolatedNodalCurve.of(EUR_DSC_META, EUR_DSC_TIME, EUR_DSC_RATE, LINEAR);
  private static final DoubleArray EUR_FWD3_TIME = DoubleArray.of(0.0, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 10.0);
  private static final DoubleArray EUR_FWD3_RATE = DoubleArray.of(0.0150, 0.0125, 0.0150, 0.0175, 0.0175, 0.0190, 0.0200, 0.0210);
  private static final CurveName EUR_FWD3_NAME = CurveName.of("EUR-FWD3");
  private static final CurveMetadata EUR_FWD3_META = Curves.zeroRates(EUR_FWD3_NAME, ACT_ACT_ISDA);
  private static final InterpolatedNodalCurve EUR_FWD3_CURVE =
      InterpolatedNodalCurve.of(EUR_FWD3_META, EUR_FWD3_TIME, EUR_FWD3_RATE, LINEAR);

  private static final DoubleArray ALPHA_TIME = DoubleArray.of(0.0, 0.5, 1.0, 2.0, 5.0, 10.0);
  private static final DoubleArray BETA_TIME = DoubleArray.of(0.0, 0.5, 1.0, 2.0, 5.0, 10.0, 100.0);
  private static final DoubleArray RHO_TIME = DoubleArray.of(0.0, 0.5, 1.0, 2.0, 5.0, 10.0, 100.0);
  private static final DoubleArray NU_TIME = DoubleArray.of(0.0, 0.5, 1.0, 2.0, 5.0, 10.0, 100.0);
  static final CurveMetadata META_ALPHA = Curves.sabrParameterByExpiry(
      "Test-SABR-Alpha", ACT_ACT_ISDA, ValueType.SABR_ALPHA).withParameterMetadata(createParameterMetadata(ALPHA_TIME));
  static final CurveMetadata META_BETA = Curves.sabrParameterByExpiry(
      "Test-SABR-Beta", ACT_ACT_ISDA, ValueType.SABR_BETA).withParameterMetadata(createParameterMetadata(BETA_TIME));
  static final CurveMetadata META_RHO = Curves.sabrParameterByExpiry(
      "Test-SABR-Rho", ACT_ACT_ISDA, ValueType.SABR_RHO).withParameterMetadata(createParameterMetadata(RHO_TIME));
  static final CurveMetadata META_NU = Curves.sabrParameterByExpiry(
      "Test-SABR-Nu", ACT_ACT_ISDA, ValueType.SABR_NU).withParameterMetadata(createParameterMetadata(NU_TIME));

  private static final DoubleArray ALPHA_VALUE_FLAT = DoubleArray.of(0.05, 0.05, 0.05, 0.05, 0.05, 0.05);
  private static final DoubleArray BETA_VALUE_FLAT = DoubleArray.of(0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
  private static final DoubleArray RHO_VALUE_FLAT = DoubleArray.of(-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25);
  private static final DoubleArray NU_VALUE_FLAT = DoubleArray.of(0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
  private static final InterpolatedNodalCurve CURVE_ALPHA_FLAT = InterpolatedNodalCurve.of(
      META_ALPHA, ALPHA_TIME, ALPHA_VALUE_FLAT, LINEAR);
  private static final InterpolatedNodalCurve CURVE_BETA_FLAT = InterpolatedNodalCurve.of(
      META_BETA, BETA_TIME, BETA_VALUE_FLAT, LINEAR);
  private static final InterpolatedNodalCurve CURVE_RHO_FLAT = InterpolatedNodalCurve.of(
      META_RHO, RHO_TIME, RHO_VALUE_FLAT, LINEAR);
  private static final InterpolatedNodalCurve CURVE_NU_FLAT = InterpolatedNodalCurve.of(
      META_NU, NU_TIME, NU_VALUE_FLAT, LINEAR);
  static final SabrParameters SABR_PARAM_FLAT = SabrParameters.of(
      CURVE_ALPHA_FLAT, CURVE_BETA_FLAT, CURVE_RHO_FLAT, CURVE_NU_FLAT, SabrVolatilityFormula.hagan());

  private static final DoubleArray ALPHA_VALUE = DoubleArray.of(0.05, 0.05, 0.05, 0.05, 0.05, 0.05);
  private static final DoubleArray BETA_VALUE = DoubleArray.of(0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
  private static final DoubleArray RHO_VALUE = DoubleArray.of(-0.25, -0.25, -0.25, -0.25, -0.25, -0.25, -0.25);
  private static final DoubleArray NU_VALUE = DoubleArray.of(0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
  private static final InterpolatedNodalCurve CURVE_ALPHA = InterpolatedNodalCurve.of(
      META_ALPHA, ALPHA_TIME, ALPHA_VALUE, LINEAR);
  private static final InterpolatedNodalCurve CURVE_BETA = InterpolatedNodalCurve.of(
      META_BETA, BETA_TIME, BETA_VALUE, LINEAR);
  private static final InterpolatedNodalCurve CURVE_RHO = InterpolatedNodalCurve.of(
      META_RHO, RHO_TIME, RHO_VALUE, LINEAR);
  private static final InterpolatedNodalCurve CURVE_NU = InterpolatedNodalCurve.of(
      META_NU, NU_TIME, NU_VALUE, LINEAR);
  private static final DoubleArray SHIFT_TIME = DoubleArray.of(0.0, 5.0, 10.0, 100.0);
  private static final DoubleArray SHIFT_VALUE = DoubleArray.of(0.01, 0.02, 0.012, 0.01);
  static final CurveMetadata META_SHIFT = DefaultCurveMetadata.builder()
      .curveName(CurveName.of("Test-SABR-Shift"))
      .xValueType(ValueType.YEAR_FRACTION)
      .parameterMetadata(createParameterMetadata(SHIFT_TIME))
      .build();
  private static final InterpolatedNodalCurve CURVE_SHIFT = InterpolatedNodalCurve.of(
      META_SHIFT, SHIFT_TIME, SHIFT_VALUE, LINEAR);
  static final SabrParameters SABR_PARAM = SabrParameters.of(
      CURVE_ALPHA, CURVE_BETA, CURVE_RHO, CURVE_NU, CURVE_SHIFT, SabrVolatilityFormula.hagan());

  static final IborCapletFloorletVolatilitiesName NAME = IborCapletFloorletVolatilitiesName.of("Test-SABR");

  // create a list of SimpleCurveParameterMetadata
  private static List<ParameterMetadata> createParameterMetadata(DoubleArray time) {
    int n = time.size();
    List<ParameterMetadata> list = new ArrayList<>();
    for (int i = 0; i < n; ++i) {
      list.add(SimpleCurveParameterMetadata.of(ValueType.YEAR_FRACTION, time.get(i)));
    }
    return list;
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains {@code ImmutableRatesProvider} for specified valuation date.
   * 
   * @param valuationDate  the valuation date
   * @return the rates provider
   */
  public static ImmutableRatesProvider getRatesProvider(LocalDate valuationDate) {
    return ImmutableRatesProvider.builder(valuationDate)
        .discountCurve(EUR, EUR_DSC_CURVE)
        .iborIndexCurve(EUR_EURIBOR_3M, EUR_FWD3_CURVE)
        .build();
  }

  /**
   * Obtains {@code SabrParametersIborCapletFloorletVolatilities} with constant SABR parameters for specified valuation date.
   * 
   * @param valuationDate  the valuation date
   * @return the volatility provider
   */
  public static SabrParametersIborCapletFloorletVolatilities getVolatilitiesFlatParameters(LocalDate valuationDate) {
    ZonedDateTime dateTime = valuationDate.atStartOfDay(ZoneOffset.UTC);
    return SabrParametersIborCapletFloorletVolatilities.of(NAME, EUR_EURIBOR_3M, dateTime, SABR_PARAM_FLAT);
  }

  /**
   * Obtains {@code SabrParametersIborCapletFloorletVolatilities} for specified valuation date.
   * 
   * @param valuationDate  the valuation date
   * @return the volatility provider
   */
  public static SabrParametersIborCapletFloorletVolatilities getVolatilities(LocalDate valuationDate) {
    ZonedDateTime dateTime = valuationDate.atStartOfDay(ZoneOffset.UTC);
    return SabrParametersIborCapletFloorletVolatilities.of(NAME, EUR_EURIBOR_3M, dateTime, SABR_PARAM);
  }

}
