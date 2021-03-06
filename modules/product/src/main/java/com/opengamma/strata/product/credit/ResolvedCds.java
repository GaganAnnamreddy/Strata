/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.product.credit;

import static com.opengamma.strata.collect.Guavate.ensureOnlyOne;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.ReferenceData;
import com.opengamma.strata.basics.StandardId;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.date.DayCount;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.product.ResolvedProduct;
import com.opengamma.strata.product.common.BuySell;

/**
 * A single-name credit default swap (CDS), resolved for pricing.
 * <p>
 * This is the resolved form of {@link Cds} and is an input to the pricers.
 * Applications will typically create a {@code ResolvedCds} from a {@code Cds}
 * using {@link Cds#resolve(ReferenceData)}.
 */
@BeanDefinition
public final class ResolvedCds
    implements ResolvedProduct, ImmutableBean, Serializable {

  /**
   * Whether the CDS is buy or sell.
   * <p>
   * A value of 'Buy' implies buying protection, where the fixed coupon is paid
   * and the protection is received  in the event of default.
   * A value of 'Sell' implies selling protection, where the fixed coupon is received
   * and the protection is paid in the event of default.
   */
  @PropertyDefinition(validate = "notNull")
  private final BuySell buySell;
  /**
   * The legal entity identifier.
   * <p>
   * This identifier is used for the reference legal entity of the CDS.
   */
  @PropertyDefinition(validate = "notNull")
  private final StandardId legalEntityId;
  /**
   * The periodic payments based on the fixed rate.
   * <p>
   * Each payment period represents part of the life-time of the leg.
   * In most cases, the periods do not overlap. However, since each payment period
   * is essentially independent the data model allows overlapping periods.
   */
  @PropertyDefinition(validate = "notEmpty")
  private final ImmutableList<CreditCouponPaymentPeriod> paymentPeriods;
  /**
   * The protection end date.
   * <p>
   * This may be different from the accrual end date of the last payment period in {@code periodicPayments}.
   */
  @PropertyDefinition(validate = "notNull")
  private final LocalDate protectionEndDate;
  /**
   * The day count convention.
   * <p>
   * This is used to convert dates to a numerical value.
   */
  @PropertyDefinition(validate = "notNull")
  private final DayCount dayCount;
  /**
   * The payment on default.
   * <p>
   * Whether the accrued premium is paid in the event of a default.
   */
  @PropertyDefinition(validate = "notNull")
  private final PaymentOnDefault paymentOnDefault;
  /**
   * The protection start of the day.
   * <p>
   * When the protection starts on the start date.
   */
  @PropertyDefinition(validate = "notNull")
  private final ProtectionStartOfDay protectionStart;
  /**
   * The number of days between valuation date and step-in date.
   * <p>
   * The step-in date is also called protection effective date. 
   * It is usually 1 calendar day for standardized CDS contracts. 
   */
  @PropertyDefinition(validate = "notNull")
  private final DaysAdjustment stepinDateOffset;
  /**
   * The number of days between valuation date and settlement date.
   * <p>
   * It is usually 3 business days for standardized CDS contracts.
   */
  @PropertyDefinition(validate = "notNull")
  private final DaysAdjustment settlementDateOffset;

  //-------------------------------------------------------------------------
  /**
   * Obtains the accrual start date.
   * <p>
   * In general this is different from the protection start date. 
   * Use {@code stepinDateOffset} to compute the protection start date.
   * 
   * @return the accrual start date
   */
  public LocalDate getAccrualStartDate() {
    return paymentPeriods.get(0).getStartDate();
  }

  /**
   * Obtains the accrual end date.
   * 
   * @return the accrual end date
   */
  public LocalDate getAccrualEndDate() {
    return paymentPeriods.get(paymentPeriods.size() - 1).getEndDate();
  }

  /**
   * Obtains the notional.
   * 
   * @return the notional
   */
  public double getNotional() {
    return paymentPeriods.get(0).getNotional();
  }

  /**
   * Obtains the currency.
   * 
   * @return the currency
   */
  public Currency getCurrency() {
    return paymentPeriods.get(0).getCurrency();
  }

  /**
   * Obtains the fixed coupon rate.
   * 
   * @return the fixed rate
   */
  public double getFixedRate() {
    return paymentPeriods.get(0).getFixedRate();
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains the effective start date from the step-in date. 
   * 
   * @param stepinDate  the step-in date
   * @return the effective start date
   */
  public LocalDate calculateEffectiveStartDate(LocalDate stepinDate) {
    LocalDate startDate = stepinDate.isAfter(getAccrualStartDate()) ? stepinDate : getAccrualStartDate();
    return protectionStart.isBeginning() ? startDate.minusDays(1) : startDate;
  }

  /**
   * Calculates the settlement date from the valuation date.
   * 
   * @param valuationDate  the valuation date
   * @param refData  the reference data to use
   * @return the settlement date
   */
  public LocalDate calculateSettlementDateFromValuation(LocalDate valuationDate, ReferenceData refData) {
    return settlementDateOffset.adjust(valuationDate, refData);
  }

  //-------------------------------------------------------------------------
  /**
   * Finds the period that contains the specified date.
   * <p>
   * The search is performed using unadjusted dates.
   * 
   * @param date  the date to find the period for
   * @return the period, empty if not found
   * @throws IllegalArgumentException if more than one period matches
   */
  public Optional<CreditCouponPaymentPeriod> findPeriod(LocalDate date) {
    return paymentPeriods.stream()
        .filter(p -> p.contains(date))
        .reduce(ensureOnlyOne());
  }

  /**
   * Calculates the accrued premium per fractional spread for unit notional.
   * 
   * @param stepinDate  the step-in date
   * @return the accrued year fraction
   */
  public double accruedYearFraction(LocalDate stepinDate) {
    if (stepinDate.isBefore(getAccrualStartDate())) {
      return 0d;
    }
    if (stepinDate.isEqual(getAccrualEndDate())) {
      return paymentPeriods.get(paymentPeriods.size() - 1).getYearFraction();
    }
    CreditCouponPaymentPeriod period = findPeriod(stepinDate)
        .orElseThrow(() -> new IllegalArgumentException("Date outside range"));
    return dayCount.relativeYearFraction(period.getStartDate(), stepinDate);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ResolvedCds}.
   * @return the meta-bean, not null
   */
  public static ResolvedCds.Meta meta() {
    return ResolvedCds.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ResolvedCds.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static ResolvedCds.Builder builder() {
    return new ResolvedCds.Builder();
  }

  private ResolvedCds(
      BuySell buySell,
      StandardId legalEntityId,
      List<CreditCouponPaymentPeriod> paymentPeriods,
      LocalDate protectionEndDate,
      DayCount dayCount,
      PaymentOnDefault paymentOnDefault,
      ProtectionStartOfDay protectionStart,
      DaysAdjustment stepinDateOffset,
      DaysAdjustment settlementDateOffset) {
    JodaBeanUtils.notNull(buySell, "buySell");
    JodaBeanUtils.notNull(legalEntityId, "legalEntityId");
    JodaBeanUtils.notEmpty(paymentPeriods, "paymentPeriods");
    JodaBeanUtils.notNull(protectionEndDate, "protectionEndDate");
    JodaBeanUtils.notNull(dayCount, "dayCount");
    JodaBeanUtils.notNull(paymentOnDefault, "paymentOnDefault");
    JodaBeanUtils.notNull(protectionStart, "protectionStart");
    JodaBeanUtils.notNull(stepinDateOffset, "stepinDateOffset");
    JodaBeanUtils.notNull(settlementDateOffset, "settlementDateOffset");
    this.buySell = buySell;
    this.legalEntityId = legalEntityId;
    this.paymentPeriods = ImmutableList.copyOf(paymentPeriods);
    this.protectionEndDate = protectionEndDate;
    this.dayCount = dayCount;
    this.paymentOnDefault = paymentOnDefault;
    this.protectionStart = protectionStart;
    this.stepinDateOffset = stepinDateOffset;
    this.settlementDateOffset = settlementDateOffset;
  }

  @Override
  public ResolvedCds.Meta metaBean() {
    return ResolvedCds.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether the CDS is buy or sell.
   * <p>
   * A value of 'Buy' implies buying protection, where the fixed coupon is paid
   * and the protection is received  in the event of default.
   * A value of 'Sell' implies selling protection, where the fixed coupon is received
   * and the protection is paid in the event of default.
   * @return the value of the property, not null
   */
  public BuySell getBuySell() {
    return buySell;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the legal entity identifier.
   * <p>
   * This identifier is used for the reference legal entity of the CDS.
   * @return the value of the property, not null
   */
  public StandardId getLegalEntityId() {
    return legalEntityId;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the periodic payments based on the fixed rate.
   * <p>
   * Each payment period represents part of the life-time of the leg.
   * In most cases, the periods do not overlap. However, since each payment period
   * is essentially independent the data model allows overlapping periods.
   * @return the value of the property, not empty
   */
  public ImmutableList<CreditCouponPaymentPeriod> getPaymentPeriods() {
    return paymentPeriods;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the protection end date.
   * <p>
   * This may be different from the accrual end date of the last payment period in {@code periodicPayments}.
   * @return the value of the property, not null
   */
  public LocalDate getProtectionEndDate() {
    return protectionEndDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the day count convention.
   * <p>
   * This is used to convert dates to a numerical value.
   * @return the value of the property, not null
   */
  public DayCount getDayCount() {
    return dayCount;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment on default.
   * <p>
   * Whether the accrued premium is paid in the event of a default.
   * @return the value of the property, not null
   */
  public PaymentOnDefault getPaymentOnDefault() {
    return paymentOnDefault;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the protection start of the day.
   * <p>
   * When the protection starts on the start date.
   * @return the value of the property, not null
   */
  public ProtectionStartOfDay getProtectionStart() {
    return protectionStart;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of days between valuation date and step-in date.
   * <p>
   * The step-in date is also called protection effective date.
   * It is usually 1 calendar day for standardized CDS contracts.
   * @return the value of the property, not null
   */
  public DaysAdjustment getStepinDateOffset() {
    return stepinDateOffset;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of days between valuation date and settlement date.
   * <p>
   * It is usually 3 business days for standardized CDS contracts.
   * @return the value of the property, not null
   */
  public DaysAdjustment getSettlementDateOffset() {
    return settlementDateOffset;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ResolvedCds other = (ResolvedCds) obj;
      return JodaBeanUtils.equal(buySell, other.buySell) &&
          JodaBeanUtils.equal(legalEntityId, other.legalEntityId) &&
          JodaBeanUtils.equal(paymentPeriods, other.paymentPeriods) &&
          JodaBeanUtils.equal(protectionEndDate, other.protectionEndDate) &&
          JodaBeanUtils.equal(dayCount, other.dayCount) &&
          JodaBeanUtils.equal(paymentOnDefault, other.paymentOnDefault) &&
          JodaBeanUtils.equal(protectionStart, other.protectionStart) &&
          JodaBeanUtils.equal(stepinDateOffset, other.stepinDateOffset) &&
          JodaBeanUtils.equal(settlementDateOffset, other.settlementDateOffset);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(buySell);
    hash = hash * 31 + JodaBeanUtils.hashCode(legalEntityId);
    hash = hash * 31 + JodaBeanUtils.hashCode(paymentPeriods);
    hash = hash * 31 + JodaBeanUtils.hashCode(protectionEndDate);
    hash = hash * 31 + JodaBeanUtils.hashCode(dayCount);
    hash = hash * 31 + JodaBeanUtils.hashCode(paymentOnDefault);
    hash = hash * 31 + JodaBeanUtils.hashCode(protectionStart);
    hash = hash * 31 + JodaBeanUtils.hashCode(stepinDateOffset);
    hash = hash * 31 + JodaBeanUtils.hashCode(settlementDateOffset);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("ResolvedCds{");
    buf.append("buySell").append('=').append(buySell).append(',').append(' ');
    buf.append("legalEntityId").append('=').append(legalEntityId).append(',').append(' ');
    buf.append("paymentPeriods").append('=').append(paymentPeriods).append(',').append(' ');
    buf.append("protectionEndDate").append('=').append(protectionEndDate).append(',').append(' ');
    buf.append("dayCount").append('=').append(dayCount).append(',').append(' ');
    buf.append("paymentOnDefault").append('=').append(paymentOnDefault).append(',').append(' ');
    buf.append("protectionStart").append('=').append(protectionStart).append(',').append(' ');
    buf.append("stepinDateOffset").append('=').append(stepinDateOffset).append(',').append(' ');
    buf.append("settlementDateOffset").append('=').append(JodaBeanUtils.toString(settlementDateOffset));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ResolvedCds}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code buySell} property.
     */
    private final MetaProperty<BuySell> buySell = DirectMetaProperty.ofImmutable(
        this, "buySell", ResolvedCds.class, BuySell.class);
    /**
     * The meta-property for the {@code legalEntityId} property.
     */
    private final MetaProperty<StandardId> legalEntityId = DirectMetaProperty.ofImmutable(
        this, "legalEntityId", ResolvedCds.class, StandardId.class);
    /**
     * The meta-property for the {@code paymentPeriods} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableList<CreditCouponPaymentPeriod>> paymentPeriods = DirectMetaProperty.ofImmutable(
        this, "paymentPeriods", ResolvedCds.class, (Class) ImmutableList.class);
    /**
     * The meta-property for the {@code protectionEndDate} property.
     */
    private final MetaProperty<LocalDate> protectionEndDate = DirectMetaProperty.ofImmutable(
        this, "protectionEndDate", ResolvedCds.class, LocalDate.class);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> dayCount = DirectMetaProperty.ofImmutable(
        this, "dayCount", ResolvedCds.class, DayCount.class);
    /**
     * The meta-property for the {@code paymentOnDefault} property.
     */
    private final MetaProperty<PaymentOnDefault> paymentOnDefault = DirectMetaProperty.ofImmutable(
        this, "paymentOnDefault", ResolvedCds.class, PaymentOnDefault.class);
    /**
     * The meta-property for the {@code protectionStart} property.
     */
    private final MetaProperty<ProtectionStartOfDay> protectionStart = DirectMetaProperty.ofImmutable(
        this, "protectionStart", ResolvedCds.class, ProtectionStartOfDay.class);
    /**
     * The meta-property for the {@code stepinDateOffset} property.
     */
    private final MetaProperty<DaysAdjustment> stepinDateOffset = DirectMetaProperty.ofImmutable(
        this, "stepinDateOffset", ResolvedCds.class, DaysAdjustment.class);
    /**
     * The meta-property for the {@code settlementDateOffset} property.
     */
    private final MetaProperty<DaysAdjustment> settlementDateOffset = DirectMetaProperty.ofImmutable(
        this, "settlementDateOffset", ResolvedCds.class, DaysAdjustment.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "buySell",
        "legalEntityId",
        "paymentPeriods",
        "protectionEndDate",
        "dayCount",
        "paymentOnDefault",
        "protectionStart",
        "stepinDateOffset",
        "settlementDateOffset");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 244977400:  // buySell
          return buySell;
        case 866287159:  // legalEntityId
          return legalEntityId;
        case -1674414612:  // paymentPeriods
          return paymentPeriods;
        case -1193325040:  // protectionEndDate
          return protectionEndDate;
        case 1905311443:  // dayCount
          return dayCount;
        case -480203780:  // paymentOnDefault
          return paymentOnDefault;
        case 2103482633:  // protectionStart
          return protectionStart;
        case 852621746:  // stepinDateOffset
          return stepinDateOffset;
        case 135924714:  // settlementDateOffset
          return settlementDateOffset;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public ResolvedCds.Builder builder() {
      return new ResolvedCds.Builder();
    }

    @Override
    public Class<? extends ResolvedCds> beanType() {
      return ResolvedCds.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code buySell} property.
     * @return the meta-property, not null
     */
    public MetaProperty<BuySell> buySell() {
      return buySell;
    }

    /**
     * The meta-property for the {@code legalEntityId} property.
     * @return the meta-property, not null
     */
    public MetaProperty<StandardId> legalEntityId() {
      return legalEntityId;
    }

    /**
     * The meta-property for the {@code paymentPeriods} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableList<CreditCouponPaymentPeriod>> paymentPeriods() {
      return paymentPeriods;
    }

    /**
     * The meta-property for the {@code protectionEndDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> protectionEndDate() {
      return protectionEndDate;
    }

    /**
     * The meta-property for the {@code dayCount} property.
     * @return the meta-property, not null
     */
    public MetaProperty<DayCount> dayCount() {
      return dayCount;
    }

    /**
     * The meta-property for the {@code paymentOnDefault} property.
     * @return the meta-property, not null
     */
    public MetaProperty<PaymentOnDefault> paymentOnDefault() {
      return paymentOnDefault;
    }

    /**
     * The meta-property for the {@code protectionStart} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ProtectionStartOfDay> protectionStart() {
      return protectionStart;
    }

    /**
     * The meta-property for the {@code stepinDateOffset} property.
     * @return the meta-property, not null
     */
    public MetaProperty<DaysAdjustment> stepinDateOffset() {
      return stepinDateOffset;
    }

    /**
     * The meta-property for the {@code settlementDateOffset} property.
     * @return the meta-property, not null
     */
    public MetaProperty<DaysAdjustment> settlementDateOffset() {
      return settlementDateOffset;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 244977400:  // buySell
          return ((ResolvedCds) bean).getBuySell();
        case 866287159:  // legalEntityId
          return ((ResolvedCds) bean).getLegalEntityId();
        case -1674414612:  // paymentPeriods
          return ((ResolvedCds) bean).getPaymentPeriods();
        case -1193325040:  // protectionEndDate
          return ((ResolvedCds) bean).getProtectionEndDate();
        case 1905311443:  // dayCount
          return ((ResolvedCds) bean).getDayCount();
        case -480203780:  // paymentOnDefault
          return ((ResolvedCds) bean).getPaymentOnDefault();
        case 2103482633:  // protectionStart
          return ((ResolvedCds) bean).getProtectionStart();
        case 852621746:  // stepinDateOffset
          return ((ResolvedCds) bean).getStepinDateOffset();
        case 135924714:  // settlementDateOffset
          return ((ResolvedCds) bean).getSettlementDateOffset();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code ResolvedCds}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<ResolvedCds> {

    private BuySell buySell;
    private StandardId legalEntityId;
    private List<CreditCouponPaymentPeriod> paymentPeriods = ImmutableList.of();
    private LocalDate protectionEndDate;
    private DayCount dayCount;
    private PaymentOnDefault paymentOnDefault;
    private ProtectionStartOfDay protectionStart;
    private DaysAdjustment stepinDateOffset;
    private DaysAdjustment settlementDateOffset;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(ResolvedCds beanToCopy) {
      this.buySell = beanToCopy.getBuySell();
      this.legalEntityId = beanToCopy.getLegalEntityId();
      this.paymentPeriods = beanToCopy.getPaymentPeriods();
      this.protectionEndDate = beanToCopy.getProtectionEndDate();
      this.dayCount = beanToCopy.getDayCount();
      this.paymentOnDefault = beanToCopy.getPaymentOnDefault();
      this.protectionStart = beanToCopy.getProtectionStart();
      this.stepinDateOffset = beanToCopy.getStepinDateOffset();
      this.settlementDateOffset = beanToCopy.getSettlementDateOffset();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 244977400:  // buySell
          return buySell;
        case 866287159:  // legalEntityId
          return legalEntityId;
        case -1674414612:  // paymentPeriods
          return paymentPeriods;
        case -1193325040:  // protectionEndDate
          return protectionEndDate;
        case 1905311443:  // dayCount
          return dayCount;
        case -480203780:  // paymentOnDefault
          return paymentOnDefault;
        case 2103482633:  // protectionStart
          return protectionStart;
        case 852621746:  // stepinDateOffset
          return stepinDateOffset;
        case 135924714:  // settlementDateOffset
          return settlementDateOffset;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 244977400:  // buySell
          this.buySell = (BuySell) newValue;
          break;
        case 866287159:  // legalEntityId
          this.legalEntityId = (StandardId) newValue;
          break;
        case -1674414612:  // paymentPeriods
          this.paymentPeriods = (List<CreditCouponPaymentPeriod>) newValue;
          break;
        case -1193325040:  // protectionEndDate
          this.protectionEndDate = (LocalDate) newValue;
          break;
        case 1905311443:  // dayCount
          this.dayCount = (DayCount) newValue;
          break;
        case -480203780:  // paymentOnDefault
          this.paymentOnDefault = (PaymentOnDefault) newValue;
          break;
        case 2103482633:  // protectionStart
          this.protectionStart = (ProtectionStartOfDay) newValue;
          break;
        case 852621746:  // stepinDateOffset
          this.stepinDateOffset = (DaysAdjustment) newValue;
          break;
        case 135924714:  // settlementDateOffset
          this.settlementDateOffset = (DaysAdjustment) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public ResolvedCds build() {
      return new ResolvedCds(
          buySell,
          legalEntityId,
          paymentPeriods,
          protectionEndDate,
          dayCount,
          paymentOnDefault,
          protectionStart,
          stepinDateOffset,
          settlementDateOffset);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets whether the CDS is buy or sell.
     * <p>
     * A value of 'Buy' implies buying protection, where the fixed coupon is paid
     * and the protection is received  in the event of default.
     * A value of 'Sell' implies selling protection, where the fixed coupon is received
     * and the protection is paid in the event of default.
     * @param buySell  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder buySell(BuySell buySell) {
      JodaBeanUtils.notNull(buySell, "buySell");
      this.buySell = buySell;
      return this;
    }

    /**
     * Sets the legal entity identifier.
     * <p>
     * This identifier is used for the reference legal entity of the CDS.
     * @param legalEntityId  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder legalEntityId(StandardId legalEntityId) {
      JodaBeanUtils.notNull(legalEntityId, "legalEntityId");
      this.legalEntityId = legalEntityId;
      return this;
    }

    /**
     * Sets the periodic payments based on the fixed rate.
     * <p>
     * Each payment period represents part of the life-time of the leg.
     * In most cases, the periods do not overlap. However, since each payment period
     * is essentially independent the data model allows overlapping periods.
     * @param paymentPeriods  the new value, not empty
     * @return this, for chaining, not null
     */
    public Builder paymentPeriods(List<CreditCouponPaymentPeriod> paymentPeriods) {
      JodaBeanUtils.notEmpty(paymentPeriods, "paymentPeriods");
      this.paymentPeriods = paymentPeriods;
      return this;
    }

    /**
     * Sets the {@code paymentPeriods} property in the builder
     * from an array of objects.
     * @param paymentPeriods  the new value, not empty
     * @return this, for chaining, not null
     */
    public Builder paymentPeriods(CreditCouponPaymentPeriod... paymentPeriods) {
      return paymentPeriods(ImmutableList.copyOf(paymentPeriods));
    }

    /**
     * Sets the protection end date.
     * <p>
     * This may be different from the accrual end date of the last payment period in {@code periodicPayments}.
     * @param protectionEndDate  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder protectionEndDate(LocalDate protectionEndDate) {
      JodaBeanUtils.notNull(protectionEndDate, "protectionEndDate");
      this.protectionEndDate = protectionEndDate;
      return this;
    }

    /**
     * Sets the day count convention.
     * <p>
     * This is used to convert dates to a numerical value.
     * @param dayCount  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder dayCount(DayCount dayCount) {
      JodaBeanUtils.notNull(dayCount, "dayCount");
      this.dayCount = dayCount;
      return this;
    }

    /**
     * Sets the payment on default.
     * <p>
     * Whether the accrued premium is paid in the event of a default.
     * @param paymentOnDefault  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder paymentOnDefault(PaymentOnDefault paymentOnDefault) {
      JodaBeanUtils.notNull(paymentOnDefault, "paymentOnDefault");
      this.paymentOnDefault = paymentOnDefault;
      return this;
    }

    /**
     * Sets the protection start of the day.
     * <p>
     * When the protection starts on the start date.
     * @param protectionStart  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder protectionStart(ProtectionStartOfDay protectionStart) {
      JodaBeanUtils.notNull(protectionStart, "protectionStart");
      this.protectionStart = protectionStart;
      return this;
    }

    /**
     * Sets the number of days between valuation date and step-in date.
     * <p>
     * The step-in date is also called protection effective date.
     * It is usually 1 calendar day for standardized CDS contracts.
     * @param stepinDateOffset  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder stepinDateOffset(DaysAdjustment stepinDateOffset) {
      JodaBeanUtils.notNull(stepinDateOffset, "stepinDateOffset");
      this.stepinDateOffset = stepinDateOffset;
      return this;
    }

    /**
     * Sets the number of days between valuation date and settlement date.
     * <p>
     * It is usually 3 business days for standardized CDS contracts.
     * @param settlementDateOffset  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder settlementDateOffset(DaysAdjustment settlementDateOffset) {
      JodaBeanUtils.notNull(settlementDateOffset, "settlementDateOffset");
      this.settlementDateOffset = settlementDateOffset;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(320);
      buf.append("ResolvedCds.Builder{");
      buf.append("buySell").append('=').append(JodaBeanUtils.toString(buySell)).append(',').append(' ');
      buf.append("legalEntityId").append('=').append(JodaBeanUtils.toString(legalEntityId)).append(',').append(' ');
      buf.append("paymentPeriods").append('=').append(JodaBeanUtils.toString(paymentPeriods)).append(',').append(' ');
      buf.append("protectionEndDate").append('=').append(JodaBeanUtils.toString(protectionEndDate)).append(',').append(' ');
      buf.append("dayCount").append('=').append(JodaBeanUtils.toString(dayCount)).append(',').append(' ');
      buf.append("paymentOnDefault").append('=').append(JodaBeanUtils.toString(paymentOnDefault)).append(',').append(' ');
      buf.append("protectionStart").append('=').append(JodaBeanUtils.toString(protectionStart)).append(',').append(' ');
      buf.append("stepinDateOffset").append('=').append(JodaBeanUtils.toString(stepinDateOffset)).append(',').append(' ');
      buf.append("settlementDateOffset").append('=').append(JodaBeanUtils.toString(settlementDateOffset));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
