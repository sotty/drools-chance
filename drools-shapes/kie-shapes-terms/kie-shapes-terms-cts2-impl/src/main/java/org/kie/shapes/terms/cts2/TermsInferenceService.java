/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.kie.shapes.terms.cts2;

import edu.mayo.cts2.framework.service.profile.conceptdomain.ConceptDomainReadService;
import edu.mayo.cts2.framework.service.profile.conceptdomainbinding.ConceptDomainBindingReadService;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQueryService;
import edu.mayo.cts2.framework.service.profile.mapentry.MapEntryReadService;
import edu.mayo.cts2.framework.service.profile.resolvedvalueset.ResolvedValueSetResolutionService;
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetReadService;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionReadService;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionResolutionService;

public interface TermsInferenceService {

    ConceptDomainReadService conceptDomainCatalogRead();

    ConceptDomainBindingReadService conceptDomainBindingRead();

    MapEntryReadService mapEntryRead();

    EntityDescriptionQueryService entityDescriptionQuery();

    ResolvedValueSetResolutionService resolvedValueSetResolution();

    ValueSetReadService valueSetCatalogRead();

    ValueSetDefinitionResolutionService valueSetDefinitionResolution();

    ValueSetDefinitionReadService valueSetDefinitionRead();

}
