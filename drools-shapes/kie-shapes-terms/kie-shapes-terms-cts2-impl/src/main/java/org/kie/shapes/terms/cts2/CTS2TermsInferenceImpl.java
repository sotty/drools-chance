package org.kie.shapes.terms.cts2;

import edu.mayo.cts2.framework.model.command.ResolvedFilter;
import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
import edu.mayo.cts2.framework.model.core.ScopedEntityName;
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI;
import edu.mayo.cts2.framework.model.service.core.NameOrURI;
import edu.mayo.cts2.framework.model.service.core.NameOrURIList;
import edu.mayo.cts2.framework.model.service.core.Query;
import edu.mayo.cts2.framework.model.util.ModelUtils;
import edu.mayo.cts2.framework.service.command.restriction.EntityDescriptionQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntitiesFromAssociationsQuery;
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQuery;
import edu.mayo.cts2.framework.service.profile.mapentry.name.MapEntryReadId;
import edu.mayo.cts2.framework.service.profile.resolvedvalueset.name.ResolvedValueSetReadId;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId;
import org.apache.commons.lang.StringUtils;
import org.kie.shapes.terms.ConceptBase;
import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ValueSet;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class CTS2TermsInferenceImpl {

	private TermsInferenceService provider = new CTS2TermsProviderImpl();

	public boolean denotes( ConceptDescriptor left, ConceptBase right, String leftPropertyUri ) {
		if ( isValueSet( right ) ) {
			// it is a valueSet
			return provider.resolvedValueSetResolution().contains( toResolvedValueSetId(right),
			                                                       asEntityNameOrURIList( left ) ).getEntryCount() > 0;
		} else if ( isValueSetDefinition( right ) ) {
			// it is a value set definition
			return provider.valueSetDefinitionResolution().contains( toValueSetDefinitionId( right ),
			                                                         asEntityNameOrURIList( left )).getEntryCount() > 0;
		} else if ( sameConceptDomain( left, right, leftPropertyUri ) ) {
			return isSelf( left, right )
					|| provider.entityDescriptionQuery().isEntityInSet( asEntityNameOrURI( left ),
					                                                    descendants( right ), getReadContext() );
		} else {
			// no other cases supported for now
			return false;
		}

	}


	private boolean isSelf(ConceptDescriptor left, ConceptBase right) {
		return left.equals( right );
	}

	private EntityDescriptionQuery descendants( ConceptBase right ) {
		EntityNameOrURI nameOrURI = new EntityNameOrURI();
		nameOrURI.setEntityName( ModelUtils.createScopedEntityName( right.getCode(), ((ConceptDescriptor)right).getCodeSystemId() ) );

		return new HierarchyEntityDescriptionQueryImpl( nameOrURI );
	}

	private ResolvedValueSetReadId toResolvedValueSetId( ConceptBase right ) {
		return new ResolvedValueSetReadId( null, ModelUtils.nameOrUriFromName( ((ValueSet )right).getValueSetId() ), null );
	}

	private ValueSetDefinitionReadId toValueSetDefinitionId( ConceptBase right ) {
		return new ValueSetDefinitionReadId( ((ValueSet)right).getValueSetId() );
	}

	private MapEntryReadId toMapEntryId( ConceptDescriptor conceptDescriptor ) {
		throw new UnsupportedOperationException("not implemented");
	}

	private URI mapIntoCodeSystem( ConceptDescriptor left, ConceptDescriptor right ) {
		//todo:
		return null;
	}

	private boolean sameCodeSystem( ConceptDescriptor left, ConceptDescriptor right ) {
		return left.getCodeSystem().equals( right.getCodeSystem() );
	}

	private boolean sameConceptDomain( ConceptDescriptor left, ConceptBase right, String leftPropertyURI ) {
		if ( leftPropertyURI == null ) {
			return true;
		}
		NameOrURI domainUri = asNameOrURI( URI.create( leftPropertyURI ) );
		if ( ! provider.conceptDomainCatalogRead().exists( domainUri, getReadContext() ) ) {
			return true;
		}

		//todo:

		return true;
	}

	private boolean isValueSetDefinition( ConceptBase right ) {
		// TODO: ValueSetDefinitionReadService not up yet
		return false;
	}

	private boolean isValueSet( ConceptBase right ) {
		return right instanceof ValueSet;
	}

	private NameOrURIList asNameOrURIList( ConceptDescriptor... cds ) {
		NameOrURIList list = new NameOrURIList();

		for(ConceptDescriptor cd : cds) {
			NameOrURI nameOrURI = asNameOrURI(cd);
			list.addEntry(nameOrURI);
		}

		return list;
	}

	private Set<EntityNameOrURI> asEntityNameOrURIList( ConceptDescriptor... cds ) {
		Set<EntityNameOrURI> nameOrURIList = new HashSet<>();
		for(ConceptDescriptor cd : cds) {
			EntityNameOrURI nameOrURI = asEntityNameOrURI(cd);
			nameOrURIList.add(nameOrURI);
		}

		return nameOrURIList;
	}

	private NameOrURI asNameOrURI( ConceptDescriptor cd ) {
		NameOrURI nameOrURI = new NameOrURI();
		nameOrURI.setUri(cd.getUri().toString());

		return nameOrURI;
	}

	private NameOrURI asNameOrURI( URI uri ) {
		NameOrURI nameOrURI = new NameOrURI();
		nameOrURI.setUri(uri.toString());

		return nameOrURI;
	}

	private EntityNameOrURI asEntityNameOrURI( URI uri ) {
		EntityNameOrURI entityNameOrURI = new EntityNameOrURI();
		entityNameOrURI.setUri(uri.toString());

		return entityNameOrURI;
	}

	private EntityNameOrURI asEntityNameOrURI( ConceptDescriptor cd ) {
		EntityNameOrURI entityNameOrURI = new EntityNameOrURI();

		if( StringUtils.isNotBlank( cd.getCode() )) {
			ScopedEntityName name = new ScopedEntityName();
			name.setName(cd.getCode());
			name.setNamespace( cd.getCodeSystemUri().toString() ) ;
			entityNameOrURI.setEntityName(name);
		} else {
			entityNameOrURI.setUri(cd.getUri().toString());
		}

		return entityNameOrURI;
	}

	private ResolvedReadContext getReadContext() {
		return null;
	}

	private static class HierarchyEntityDescriptionQueryImpl implements EntityDescriptionQuery {

		private EntityNameOrURI entity;

		private HierarchyEntityDescriptionQueryImpl(EntityNameOrURI entity) {
			super();
			this.entity = entity;
		}

		@Override
		public EntitiesFromAssociationsQuery getEntitiesFromAssociationsQuery() {
			return null;
		}

		@Override
		public EntityDescriptionQueryServiceRestrictions getRestrictions() {
			EntityDescriptionQueryServiceRestrictions restrictions = new EntityDescriptionQueryServiceRestrictions();

			EntityDescriptionQueryServiceRestrictions.HierarchyRestriction hierarchyRestriction = new EntityDescriptionQueryServiceRestrictions.HierarchyRestriction();
			hierarchyRestriction.setHierarchyType(EntityDescriptionQueryServiceRestrictions.HierarchyRestriction.HierarchyType.DESCENDANTS);

			hierarchyRestriction.setEntity(this.entity);

			restrictions.setHierarchyRestriction(hierarchyRestriction);

			return restrictions;
		}

		@Override
		public Query getQuery() {
			return null;
		}

		@Override
		public Set<ResolvedFilter> getFilterComponent() {
			return null;
		}

		@Override
		public ResolvedReadContext getReadContext() {
			return null;
		}
	}


}
